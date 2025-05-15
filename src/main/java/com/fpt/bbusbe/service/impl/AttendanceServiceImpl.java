package com.fpt.bbusbe.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.firebase.FirebaseMessagingService;
import com.fpt.bbusbe.model.dto.db.AttendanceDTO;
import com.fpt.bbusbe.model.dto.db.AttendanceFirebaseDto;
import com.fpt.bbusbe.model.dto.request.attendance.ManualAttendanceRequest;
import com.fpt.bbusbe.model.dto.response.attendance.*;
import com.fpt.bbusbe.model.dto.response.bus.BusReport;
import com.fpt.bbusbe.model.dto.response.route.RouteReport;
import com.fpt.bbusbe.model.entity.Attendance;
import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusDirection;
import com.fpt.bbusbe.model.enums.StudentStatus;
import com.fpt.bbusbe.model.enums.UserStatus;
import com.fpt.bbusbe.repository.*;
import com.fpt.bbusbe.service.AttendanceService;
import com.fpt.bbusbe.service.S3Service;
import com.fpt.bbusbe.utils.DateTimeUtils;
import com.fpt.bbusbe.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final S3Service s3Service;
    private final FirebaseMessagingService firebaseMessagingService;
    private final ObjectMapper objectMapper;
    private final StudentRepository studentRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public List<Attendance> findAllByStudent_Checkpoint_Route_BusSchedules_Bus_IdAndDirection(UUID busId, BusDirection direction) {
//        return attendanceRepository.findAllByStudent_Checkpoint_Route_BusSchedules_Bus_IdAndDirection(busId, direction);
//    }

    @Override
    public AttendanceDTO convertToDTO(Attendance attendance) {
        return AttendanceDTO.builder()
                .studentId(attendance.getStudent().getId())
                .date(attendance.getDate())
                .direction(attendance.getDirection())
                .checkpointId(attendance.getCheckpoint() == null ? null : attendance.getCheckpoint().getId())
                .busId(attendance.getBus() == null ? null : attendance.getBus().getId())
                .status(attendance.getStatus())
                .checkin(attendance.getCheckin())
                .checkout(attendance.getCheckout())
                .build();
    }

    @Override
    public List<AttendanceResponse> findAllByBusIdAndDateAndDirection(UUID busId, LocalDate date, BusDirection direction) {
        List<Attendance> attendances = attendanceRepository.findAllByBusIdAndDateAndDirectionOrderByStudent_Checkpoint_Name(busId, date, direction);
        return attendances.stream()
                .map(attendance -> AttendanceResponse.builder()
                        .id(attendance.getId())
                        .studentId(attendance.getStudent().getId())
                        .rollNumber(attendance.getStudent().getRollNumber())
                        .studentName(attendance.getStudent().getName())
                        .avatarUrl(s3Service.generatePresignedUrl("students/" + attendance.getStudent().getAvatar()))
                        .dob(attendance.getStudent().getDob())
                        .direction(attendance.getDirection())
                        .checkpointId(attendance.getCheckpoint() == null ? null : attendance.getCheckpoint().getId())
                        .checkpointName(attendance.getCheckpoint() == null ? null : attendance.getCheckpoint().getName())
                        .parentName(attendance.getStudent().getParent().getUser().getName())
                        .parentPhone(attendance.getStudent().getParent().getUser().getPhone())
                        .status(attendance.getStatus())
                        .checkin(attendance.getCheckin())
                        .checkout(attendance.getCheckout())
                        .build())
                .toList();
    }

    @Override
    public List<AttendanceResponseForAdmin> getAttendanceHistoryOfAStudent(UUID studentId) {
        List<Object[]> results = attendanceRepository.findAllByStudentIdReturnWithDriverNameAndAssistant(studentId);
        return results.stream().map(obj -> AttendanceResponseForAdmin.builder()
                .id((UUID) obj[0])
                .direction(BusDirection.valueOf((String) obj[1]))
                .status(AttendanceStatus.valueOf((String) obj[2]))
                .routeDescription((String) obj[3])
                .date(((java.sql.Date) obj[4]).toLocalDate())
                .checkin((Date) obj[5])
                .checkout((Date) obj[6])
                .driverName((String) obj[7])
                .assistantName((String) obj[8])
                .checkpointName((String) obj[9])
                .modifiedBy(processModifiedBy((String) obj[10]))
                .routeCode((String) obj[11])
                .build()   // registered
        ).collect(Collectors.toList());
    }

    private String processModifiedBy(String modifiedBy) {
        if (modifiedBy == null) return "";
        if (modifiedBy.equals("camera")) return "Nhận diện tự động qua camera";
        else return "Điểm danh thủ công bởi phụ xe";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualAttendance(ManualAttendanceRequest req) {
        Attendance attendance = attendanceRepository.findById(req.getAttendanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));

        boolean updated = false;

        if (req.getCheckin() != null) {
            attendance.setCheckin(req.getCheckin());
            attendance.setStatus(AttendanceStatus.IN_BUS);
            updated = true;
        }

        if (req.getCheckout() != null) {
            attendance.setCheckout(req.getCheckout());
            attendance.setStatus(AttendanceStatus.ATTENDED);
            updated = true;
        }

        if (updated) {
            // Lưu thông tin người sửa (assistant đang đăng nhập)
            UUID userLoggedInId = TokenUtils.getUserLoggedInId();
            attendance.setModifiedBy("Điểm danh thủ công bởi phụ xe: " + userLoggedInId.toString());
            attendance = attendanceRepository.save(attendance);
        } else {
            throw new InvalidDataException("Phải truyền ít nhất một trong hai trường checkin hoặc checkout");
        }

        try {
            String title;

            AttendanceFirebaseDto attendanceMessage = AttendanceFirebaseDto.builder()
                    .studentId(attendance.getStudent().getId())
                    .studentName(attendance.getStudent().getName())
                    .direction(attendance.getDirection())
                    .status(attendance.getStatus())
                    .modifiedBy(attendance.getModifiedBy())
                    .build();

            if (attendance.getStatus() == AttendanceStatus.IN_BUS) {
                Date time = attendance.getCheckin();
                //Convert from util.date to "yyyy-MM-dd HH:mm:ss" format
                String timeString = DateTimeUtils.convertDateToString(time, "yyyy-MM-dd HH:mm:ss");
                attendanceMessage.setTime(timeString);
                title = "Con của bạn đã lên xe";
            } else {
                Date time = attendance.getCheckout();
                //Convert from util.date to "yyyy-MM-dd HH:mm:ss" format
                String timeString = DateTimeUtils.convertDateToString(time, "yyyy-MM-dd HH:mm:ss");
                attendanceMessage.setTime(timeString);
                title = "Con của bạn đã xuống xe";
            }
            String message = objectMapper.writeValueAsString(attendanceMessage);
            //Send notification to parent
            String deviceId = attendance.getStudent().getParent().getUser().getDeviceToken();
            firebaseMessagingService.sendNotificationToSpecificUser(
                    deviceId,
                    title,
                    message
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<AttendanceResponse> getAttendanceHistoryOfAStudentForParent(UUID studentId, LocalDate date) {
        log.info("Get attendance of a student for parent");

        List<Attendance> attendances = attendanceRepository.findAllByStudent_IdAndDate(studentId, date);
        attendances.sort((o1, o2) -> o1.getDirection().compareTo(o2.getDirection()));

        if (attendances.isEmpty()) {
            throw new ResourceNotFoundException("Attendance not found");
        }

        return attendances.stream().map(AttendanceResponse::new).collect(Collectors.toList());

    }

    @Override
    public List<FinalReport> generateFinalReport() {
        List<Object[]> results = attendanceRepository.finalReport();
        return results.stream().map(obj -> FinalReport.builder()
                .grade(Integer.parseInt((String) obj[0]))
                .amountOfStudentRegistered((long) obj[1])
                .amountOfStudentDeregistered((long) obj[2])
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<BusReport> generateBusReport() {
        List<Object[]> results = attendanceRepository.busReport();
        return results.stream().map(obj -> BusReport.builder()
                .licensePlate((String) obj[0])
                .driverName((String) obj[1])
                .assistantName((String) obj[2])
                .amountOfRide((long) obj[3])
                .maxCapacity((int) obj[4])
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<RouteReport> generateRouteReport() {
        List<Object[]> results = attendanceRepository.routeReport();
        return results.stream().map(obj -> RouteReport.builder()
                .routeName((String) obj[0])
                .path((String) obj[1])
                .amountOfStudent((long) obj[2])
                .amountOfTrip((long) obj[3])
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceReport> generateAttendanceReport() {
        List<Object[]> results = attendanceRepository.attendanceReport();
        return results.stream().map(obj -> AttendanceReport.builder()
                .studentName((String) obj[0])
                .rollNumber((String) obj[1])
                .checkInNumber((long) obj[2])
                .checkOutNumber((long) obj[3])
                .totalCheckNumber((long) obj[4])
                .note((String) obj[5])
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<DriverAndAssistantReport> generateDriverAndAssistantReport() {
        List<Object[]> results = attendanceRepository.driverAndAssistantReport();
        return results.stream().map(obj -> DriverAndAssistantReport.builder()
                .driverName((String) obj[0])
                .assistantName((String) obj[1])
                .numberOfManualAttendance((long) obj[2])
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public DashBoard dashboard() {
        DashBoard dashBoard = new DashBoard();
        dashBoard.setTotalActiveStudent(studentRepository.countByStatus(StudentStatus.ACTIVE));
        dashBoard.setTotalActiveRoute(routeRepository.count());
        dashBoard.setTotalActiveUser(userRepository.countByStatus(UserStatus.ACTIVE));

        LocalDate today = LocalDate.now();

// Determine academic year start and end based on whether today is before or after July
        int year = today.getYear();
        if (today.getMonthValue() > 7) { // After July (August or later)
            year += 1;
        }

        LocalDate startDate = LocalDate.of(year - 1, Month.SEPTEMBER, 1);
        LocalDate endDate = LocalDate.of(year, Month.JUNE, 1);

        attendanceRepository.findAttendanceRate(startDate, endDate);
        List<Object[]> results = attendanceRepository.findAttendanceRate(startDate, endDate);
        List<DashBoard.AttendanceRate> attendanceRates = results.stream().map(obj -> DashBoard.AttendanceRate.builder()
                .month((String) obj[0])
                .percentage((BigDecimal) obj[1])
                .build()
        ).collect(Collectors.toList());
        dashBoard.setAttendanceRate(attendanceRates);
        return dashBoard;
    }

}
