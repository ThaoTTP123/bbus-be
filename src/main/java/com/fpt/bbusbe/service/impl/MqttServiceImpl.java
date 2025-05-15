package com.fpt.bbusbe.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.bbusbe.config.WebSocketHandler;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.firebase.FirebaseMessagingService;
import com.fpt.bbusbe.model.dto.db.AttendanceFirebaseDto;
import com.fpt.bbusbe.model.dto.response.attendance.AttendanceResponse;
import com.fpt.bbusbe.model.entity.*;
import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusDirection;
import com.fpt.bbusbe.model.enums.CameraRequestType;
import com.fpt.bbusbe.model.mqtt.AttendanceMessage;
import com.fpt.bbusbe.model.mqtt.PersonListMqttJson;
import com.fpt.bbusbe.model.mqtt.OperationType;
import com.fpt.bbusbe.model.mqtt.ResultMessage;
import com.fpt.bbusbe.mqtt.MqttPublisherService;
import com.fpt.bbusbe.repository.*;
import com.fpt.bbusbe.service.MqttService;
import com.fpt.bbusbe.service.S3Service;
import com.fpt.bbusbe.utils.DateTimeUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j(topic = "MQTT-SERVICE")
@RequiredArgsConstructor
public class MqttServiceImpl implements MqttService {


    private final CameraRepository cameraRepository;
    private final MqttPublisherService mqttPublisherService;
    private final S3Service s3Service;
    private final CameraRequestRepository cameraRequestRepository;
    private final CameraRequestDetailRepository cameraRequestDetailRepository;
    private final StudentRepository studentRepository;
    private final BusRepository busRepository;
    private final AttendanceRepository attendanceRepository;
    private final WebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;
    private final FirebaseMessagingService firebaseMessagingService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void publishStudentsList(List<Student> students, OperationType operationType, String facesluice) {
        String type = operationType.getOperationType();
        log.info("Sending students list to MQTT with type: {}", type);
        Camera camera = cameraRepository.findByFacesluice(facesluice);
        if (camera == null) {
            log.error("Camera not found with facesluice: {}", facesluice);
        }
        CameraRequest cameraRequest = new CameraRequest();
        switch (operationType) {
            case ADD:
                cameraRequest.setRequestType(CameraRequestType.ADD);
                break;
            case DELETE:
                cameraRequest.setRequestType(CameraRequestType.DELETE);
                break;
            case EDIT:
                cameraRequest.setRequestType(CameraRequestType.EDIT);
                break;
            default:
                throw new IllegalArgumentException("Invalid operation type: " + operationType);
        }
        cameraRequest.setCamera(camera);

        cameraRequest = cameraRequestRepository.save(cameraRequest);
        List<CameraRequestDetail> cameraRequestDetails = new ArrayList<>();

        for (Student student : students) {
            CameraRequestDetail.CameraRequestDetailId cameraRequestDetailId =
                    CameraRequestDetail.CameraRequestDetailId.builder()
                            .cameraRequest(cameraRequest)
                            .student(student)
                            .build();

            CameraRequestDetail cameraRequestDetail = CameraRequestDetail.builder()
                    .id(cameraRequestDetailId)
                    .rollNumber(student.getRollNumber())
                    .name(student.getName())
                    .personType(0)
                    .avatar(student.getAvatar())
                    .errCode(1)
                    .build();
            cameraRequestDetails.add(cameraRequestDetail);
        }

        cameraRequestDetailRepository.saveAll(cameraRequestDetails);

        List<PersonListMqttJson.PersonInfo> personInfos = new ArrayList<>();
        String format = "yyyy-MM-dd'T'HH:mm:ss";
        //Get the LocalDateTime of the beginning of September 1st this year
        LocalDateTime firstSep = LocalDateTime.of(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.SEPTEMBER,
                1,
                0,
                0,
                0
        );
        String cardValidBegin = firstSep.format(DateTimeFormatter.ofPattern(format));
        String cardValidEnd = firstSep.plusYears(1).format(DateTimeFormatter.ofPattern(format));
        for (Student student : students) {
            String avatarUrl = "";
            try {
                avatarUrl = s3Service.generatePresignedUrl("students/" + student.getAvatar());
            } catch (Exception e) {
                log.error("Error generating presigned URL for student avatar: {}", e.getMessage());
            }
            PersonListMqttJson.PersonInfo personInfo = new PersonListMqttJson.PersonInfo();
            if (operationType == OperationType.ADD) {
                personInfo = PersonListMqttJson.PersonInfo.builder()
                        .customId(student.getId().toString())
                        .name(student.getName())
                        .personType(0)
                        .tempCardType(0)
                        .cardValidBegin(cardValidBegin)
                        .cardValidEnd(cardValidEnd)
                        .picURI(avatarUrl)
                        .build();
            }

            personInfos.add(personInfo);
        }
        if (personInfos.isEmpty()) return;
        PersonListMqttJson studentJson = PersonListMqttJson.builder()
                .messageId(type + "List" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")))
                .dataBegin("BeginFlag")
                .operator(type)
                .personNum(String.valueOf(personInfos.size()))
                .info(personInfos)
                .dataEnd("EndFlag")
                .build();
        try {
            mqttPublisherService.publishMessage(facesluice, studentJson);
        } catch (MqttException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void handleAttendanceMessage(AttendanceMessage message, String topic) throws JsonProcessingException {
        log.info("handleAttendanceMessage");
        Student student = studentRepository.findById(UUID.fromString(message.getInfo().getCustomId())).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        String title = "";
        if (student == null) {
            log.error("Student not found");
            return;
        }
        Bus bus = busRepository.findByCamera_Facesluice(getCameraFacesluiceFromTopicRec(topic));
        if (bus == null) {
            log.error("Bus not found");
            return;
        }

        //message.getInfo().getTime() 2025-03-31 18:18:54
        String timeString = message.getInfo().getTime();
        Date time = DateTimeUtils.convertStringToDate(timeString, "yyyy-MM-dd HH:mm:ss");
        LocalDate date = LocalDate.now();

        //Check if student has already checked in and checkout today
        Attendance attendance = attendanceRepository.findByStudent_IdAndDateAndDirection(student.getId(), date, isNotOverNoon(timeString) ? BusDirection.PICK_UP : BusDirection.DROP_OFF);
        if (attendance == null) {
            attendance = Attendance.builder()
                    .student(student)
                    .date(date)
                    .checkpoint(student.getCheckpoint())
                    .direction(isNotOverNoon(timeString) ? BusDirection.PICK_UP : BusDirection.DROP_OFF)
                    .status(AttendanceStatus.ABSENT)
                    .bus(bus)
                    .build();
        }
        if (attendance.getCheckin() != null && attendance.getCheckout() == null) {
            log.info("Student has already checked in today");
            //Compare if the time is over the time checkin by 2 minute
            long checkinTime = attendance.getCheckin().getTime();
            long currentTime = time.getTime();
            if (currentTime - checkinTime < 5000) return;
            attendance.setCheckout(time);
            attendance.setStatus(AttendanceStatus.ATTENDED);
            title = "Con của bạn đã xuống xe";
        } else if (attendance.getCheckin() == null) {
            log.info("Student has not checked in today");
            attendance.setCheckin(time);
            attendance.setStatus(AttendanceStatus.IN_BUS);
            attendance.setModifiedBy("camera");
            title = "Con của bạn đã lên xe";
        } else log.error("Student has already checked out today");

        attendance = attendanceRepository.save(attendance);
        UUID busId = bus.getId();

        AttendanceResponse attendanceResponse = new AttendanceResponse(attendance);
        webSocketHandler.publishStudentStatus(busId + "/" + (isNotOverNoon(timeString) ? "0" : "1") + "/student-status", attendanceResponse);
        String deviceToken = student.getParent().getUser().getDeviceToken();
        if (deviceToken == null || deviceToken.isEmpty()) {
            throw new ResourceNotFoundException("Device token not found");
        }
        //Build AttendanceMessage to send to parent
        AttendanceFirebaseDto attendanceSendMessage = AttendanceFirebaseDto.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .status(attendance.getStatus())
                .direction(attendance.getDirection())
                .time(timeString)
                .modifiedBy("Điểm danh tự động bằng camera")
                .pic(message.getInfo().getPic())
                .build();
        try {
            String jsonMessage = objectMapper.writeValueAsString(attendanceSendMessage);
            firebaseMessagingService.sendNotificationToSpecificUser(
                    student.getParent().getUser().getDeviceToken(),
                    title,
                    jsonMessage
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON", e);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void handleResultMessage(ResultMessage resultMessage, String topicReceive) {
        String facesluice = getCameraFacesluiceFromTopicAck(topicReceive);
        List<ResultMessage.AddErrInfo> studentIdsWithReturnCode = resultMessage.getInfo().getAddErrInfos();
        if (studentIdsWithReturnCode == null) {
            studentIdsWithReturnCode = new ArrayList<>();
        }
        for (ResultMessage.AddSucInfo addSucInfo : resultMessage.getInfo().getAddSucInfos()) {
            studentIdsWithReturnCode.add(new ResultMessage.AddErrInfo(addSucInfo.getCustomId(), 0));
        }
        //Update the camera request detail with its return code by facesluice and student id
        CameraRequest cameraRequest = cameraRequestRepository.findTopByCamera_FacesluiceOrderByCreatedAtDesc(facesluice);
        for (ResultMessage.AddErrInfo addErrInfo : studentIdsWithReturnCode) {
            //find the latest camera request detail by camera id
            CameraRequestDetail.CameraRequestDetailId cameraRequestDetailId =
                    CameraRequestDetail.CameraRequestDetailId.builder()
                            .cameraRequest(cameraRequest)
                            .student(studentRepository.findById(UUID.fromString(addErrInfo.getCustomId())).orElseThrow(() -> new ResourceNotFoundException("Student not found")))
                            .build();
            CameraRequestDetail cameraRequestDetail = cameraRequestDetailRepository.findById(cameraRequestDetailId).orElseThrow(() -> new ResourceNotFoundException("Camera request detail not found"));
            cameraRequestDetail.setErrCode(addErrInfo.getErrCode());
            cameraRequestDetailRepository.save(cameraRequestDetail);
        }
    }

    //Get the camera facesluice from the topic and find the bus with that camera
    private static String getCameraFacesluiceFromTopicRec(String topic) {
        //"mqtt/face/\\d+/Rec"
        return topic.substring(
                topic.lastIndexOf("/", topic.lastIndexOf("/Rec") - 1) + 1,
                topic.lastIndexOf("/Rec")
        );
    }

    private static String getCameraFacesluiceFromTopicAck(String topic) {
        //"mqtt/face/\\d+/Rec"
        return topic.substring(
                topic.lastIndexOf("/", topic.lastIndexOf("/Ack") - 1) + 1,
                topic.lastIndexOf("/Ack")
        );
    }

    private static boolean isNotOverNoon(String time) {
        // Define the date-time formatter for the input format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse the input string into a LocalDateTime object
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);

        // Check if the hour is greater than or equal to 12 (noon)
        return dateTime.getHour() < 12;
    }
}
