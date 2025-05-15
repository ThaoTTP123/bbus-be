package com.fpt.bbusbe.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.bbusbe.model.dto.response.cameraRequest.CameraRequestPageResponse;
import com.fpt.bbusbe.model.dto.response.cameraRequest.CameraRequestResponse;
import com.fpt.bbusbe.model.dto.response.user.UserPageResponse;
import com.fpt.bbusbe.model.dto.response.user.UserResponse;
import com.fpt.bbusbe.model.entity.CameraRequest;
import com.fpt.bbusbe.model.entity.CameraRequestDetail;
import com.fpt.bbusbe.model.entity.Student;
import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.model.mqtt.OperationType;
import com.fpt.bbusbe.repository.CameraRequestDetailRepository;
import com.fpt.bbusbe.repository.CameraRequestRepository;
import com.fpt.bbusbe.service.CameraRequestService;
import com.fpt.bbusbe.service.MqttService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j(topic = "CAMERA-REQUEST-SERVICE")
@RequiredArgsConstructor
public class CameraRequestServiceImpl implements CameraRequestService {
    private final CameraRequestRepository cameraRequestRepository;
    private final CameraRequestDetailRepository cameraRequestDetailRepository;
    private final MqttService mqttService;

    @Override
    public CameraRequestPageResponse findAll(String keyword, String sort, int page, int size) {
        log.info("findAll start");

        // Sorting
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // tencot:asc|desc
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    order = new Sort.Order(Sort.Direction.ASC, columnName);
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, columnName);
                }
            }
        }

        // Xử lý trường hợp FE muốn bắt đầu với page = 1
        int pageNo = (page > 0) ? page - 1 : 0;

        // Paging
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        String formattedKeyword = (StringUtils.hasLength(keyword)) ? "%" + keyword.toLowerCase() + "%" : "%";

        Page<CameraRequest> entityPage;

        if (formattedKeyword.equals("success")) {
            entityPage = cameraRequestRepository.findAllByStatus(true, pageable);
        } else if (formattedKeyword.equals("error")) {
            entityPage = cameraRequestRepository.findAllByStatus(false, pageable);
        } else{
            entityPage = cameraRequestRepository.findAllByCamera_Facesluice(formattedKeyword, pageable);
        }

        return getCameraRequestPageResponse(page, size, entityPage);
    }

    private CameraRequestPageResponse getCameraRequestPageResponse(int page, int size, Page<CameraRequest> entityPage) {
        log.info("Convert Camera Request Entity Page");

        List<CameraRequestResponse> cameraRequestResponses = entityPage.stream().map(entity -> {
            List<CameraRequestResponse.CameraRequestDetail> cameraRequestDetails = new ArrayList<>();

                    for (CameraRequestDetail detail : entity.getCameraRequestDetails()) {
                    cameraRequestDetails.add(
                            new CameraRequestResponse.CameraRequestDetail(detail)
                    );
                    }

                    return CameraRequestResponse.builder()
                            .cameraRequestId(entity.getId())
                            .requestType(entity.getRequestType())
                            .status(entity.getStatus())
                            .requests(cameraRequestDetails)
                            .build();
                }
        ).toList();

        CameraRequestPageResponse response = new CameraRequestPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(entityPage.getTotalElements());
        response.setTotalPages(entityPage.getTotalPages());
        response.setCameras(cameraRequestResponses);

        return response;
    }

    @Override
    public CameraRequestResponse findById(UUID id) {
        return null;
    }

    @Override
    public void save(UUID cameraRequestId) {

    }

    @Override
    public void uploadAllUnsuccessfulCameraRequestDetails() throws JsonProcessingException {
        List<Object[]> cameraRequestDetails = cameraRequestDetailRepository.findLatestUnsuccessfulInsertion();
        String faceSluiceId = "";
        List<Student> students = new ArrayList<>();
        for (Object[] cameraRequestDetail : cameraRequestDetails) {
            if(cameraRequestDetail[0] == cameraRequestDetails.get(0)[0])
                faceSluiceId = (String) cameraRequestDetail[5];

            if(!(faceSluiceId.equals(cameraRequestDetail[5]))){
                mqttService.publishStudentsList(students, OperationType.ADD, faceSluiceId);
                faceSluiceId = (String) cameraRequestDetail[5];
                students = new ArrayList<>();
            }
            Student student = new Student();
            student.setId((UUID) cameraRequestDetail[0]);
            student.setAvatar((String) cameraRequestDetail[1]);
            students.add(student);
            if(cameraRequestDetail[0].equals(cameraRequestDetails.get(cameraRequestDetails.size() - 1)[0])){
                mqttService.publishStudentsList(students, OperationType.ADD, faceSluiceId);
            }
        }
    }
}
