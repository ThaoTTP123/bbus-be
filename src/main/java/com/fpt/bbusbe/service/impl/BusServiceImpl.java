package com.fpt.bbusbe.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.bbusbe.config.WebSocketHandler;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.firebase.FirebaseMessagingService;
import com.fpt.bbusbe.model.dto.request.bus.BusCreationRequest;
import com.fpt.bbusbe.model.dto.request.bus.BusStatusChangeRequest;
import com.fpt.bbusbe.model.dto.request.bus.BusUpdateRequest;
import com.fpt.bbusbe.model.dto.response.attendance.AttendanceResponse;
import com.fpt.bbusbe.model.dto.response.bus.BusPageResponse;
import com.fpt.bbusbe.model.dto.response.bus.BusResponse;
import com.fpt.bbusbe.model.entity.*;
import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusDirection;
import com.fpt.bbusbe.model.enums.BusStatus;
import com.fpt.bbusbe.model.mqtt.AttendanceMessage;
import com.fpt.bbusbe.model.mqtt.BusLocationMessage;
import com.fpt.bbusbe.repository.*;
import com.fpt.bbusbe.service.BusService;
import com.fpt.bbusbe.service.S3Service;
import com.fpt.bbusbe.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j(topic = "BUS-SERVICE")
@Service
@RequiredArgsConstructor
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;
    private final AssistantRepository assistantRepository;
    private final DriverRepository driverRepository;
    private final RouteRepository routeRepository;
    private final CameraRepository cameraRepository;
    private final WebSocketHandler webSocketHandler;
    private final FirebaseMessagingService firebaseMessagingService;
    private final ObjectMapper objectMapper;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final S3Service s3Service;

    @Override
    public List<BusResponse> findBusesByCheckpointId(UUID checkpointId) {
        List<Bus> buses = busRepository.findAllByCheckpointId(checkpointId);
        return buses.stream().map(BusServiceImpl::getBusResponse).toList();
    }

    @Override
    public BusPageResponse findAll(String keyword, String sort, int page, int size) {
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

        // Chuẩn hóa `keyword` (nếu có) để tránh lỗi query
        String formattedKeyword = (StringUtils.hasLength(keyword)) ? "%" + keyword.toLowerCase() + "%" : "%";

        Page<Bus> entityPage = busRepository.searchByKeyword(formattedKeyword, pageable);
        return getBusPageResponse(page, size, entityPage);
    }

    @Override
    public BusResponse findById(UUID id) {
        Bus bus = busRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bus not found"));
        return getBusResponse(bus);
    }

    @Override
    public List<BusResponse> findBusesByRouteId(UUID routeId) {
        List<Bus> buses = busRepository.findAllByRoute_Id(routeId);
        return buses.stream().map(BusServiceImpl::getBusResponse).toList();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusResponse save(BusCreationRequest req) {
        Assistant assistant = assistantRepository.findByUser_Phone(req.getAssistantPhone());
        Driver driver = driverRepository.findByUser_Phone(req.getDriverPhone());
        Route route = routeRepository.findById(req.getRoute()).orElseThrow(() -> new ResourceNotFoundException("Route not found"));
        Camera camera = cameraRepository.findByFacesluice("1" + getEspId(req.getName()));
        if(camera == null){
            camera = Camera.builder()
                    .facesluice("1" + getEspId(req.getName()))
                    .build();
        }
        Bus bus = Bus.builder()
                .name(req.getName())
                .licensePlate(req.getLicensePlate())
                .assistant(assistant)
                .driver(driver)
                .route(route)
                .status(BusStatus.ACTIVE)
                .camera(camera)
                .espId(getEspId(req.getName()))
                .build();

        bus = busRepository.save(bus);
        camera.setBus(bus);
        cameraRepository.save(camera);
        return getBusResponse(bus);
    }

    @Override
    public void updateMaxCapacity(int maxCapacity) {
        Bus bus = busRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000000")).orElseThrow(() -> new ResourceNotFoundException("Bus not found"));
        bus.setMaxCapacity(maxCapacity);
        busRepository.save(bus);
    }

    static String getEspId(String busName){
        String numberPart = busName.replaceAll("\\D+", "");
        if(numberPart.isEmpty()) throw new IllegalArgumentException("Bus name must contain number part");
        int number = Integer.parseInt(numberPart);
        return String.format("%03d", number) + "001";
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusResponse update(BusUpdateRequest req) {
        UUID id = req.getId();

        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        // Update Assistant (optional, no errors)
        if (req.getAssistantPhone() != null && !req.getAssistantPhone().isEmpty()) {
            Assistant assistant = assistantRepository.findByUser_Phone(req.getAssistantPhone());
            if (assistant != null) {
                bus.setAssistant(assistant);
            }
        }

        // Update Driver (optional, no errors)
        if (req.getDriverPhone() != null && !req.getDriverPhone().isEmpty()) {
            Driver driver = driverRepository.findByUser_Phone(req.getDriverPhone());
            if (driver != null) {
                bus.setDriver(driver);
            }
        }

        // Update License Plate (optional, no errors)
        if (req.getLicensePlate() != null && !req.getLicensePlate().isEmpty()) {
            bus.setLicensePlate(req.getLicensePlate());
        }

        bus = busRepository.save(bus);
        return getBusResponse(bus);
    }


    @Override
    public void delete(UUID id) {
        busRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(BusStatusChangeRequest req) {
        Bus bus = busRepository.findById(req.getId()).orElseThrow(() -> new ResourceNotFoundException("Bus not found"));
        bus.setStatus(req.getStatus());
        busRepository.save(bus);
    }

    public void handleBusLocationMessage(BusLocationMessage busLocationMessage, String topic){
        //Find espId from topic
        String espId = topic.substring(
                topic.lastIndexOf("/", topic.lastIndexOf("/location") - 1) + 1,
                topic.lastIndexOf("/location")
        );

        //Find if bus exists
        Bus bus = busRepository.findByEspId(espId);
        if (bus == null) {
            throw new ResourceNotFoundException("Bus not found");
        }

        sendMessageToWebSocket(espId, busLocationMessage.toString());
    }

    private void sendMessageToWebSocket(String espId, String jsonString) {
        try {
            webSocketHandler.broadcast(espId, jsonString);
            log.info("Message sent to busId " + espId + ": " + jsonString);
        } catch (Exception e) {
            log.info("Failed to send message to WebSocket for busId: " + espId);
        }
    }

    private static BusPageResponse getBusPageResponse(int page, int size, Page<Bus> busEntities) {
        log.info("Convert Bus Entity Page");

        List<BusResponse> busList = busEntities.stream().map(BusServiceImpl::getBusResponse).toList();

        BusPageResponse response = new BusPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(busEntities.getTotalElements());
        response.setTotalPages(busEntities.getTotalPages());
        response.setBuses(busList);

        return response;
    }

    public static BusResponse getBusResponse(Bus bus) {
        return BusResponse.builder()
                .id(bus.getId())
                .name(bus.getName())
                .licensePlate(bus.getLicensePlate())
                .espId(bus.getEspId())
                .assistantId(bus.getAssistant() == null ? null : bus.getAssistant().getId())
                .assistantName(bus.getAssistant() == null ? null : bus.getAssistant().getUser().getName())
                .assistantPhone(bus.getAssistant() == null ? null : bus.getAssistant().getUser().getPhone())
                .driverId(bus.getDriver() == null ? null : bus.getDriver().getId())
                .driverName(bus.getDriver() == null ? null : bus.getDriver().getUser().getName())
                .driverPhone(bus.getDriver() == null ? null : bus.getDriver().getUser().getPhone())
                .routeId(bus.getRoute() == null ? null : bus.getRoute().getId())
                .routeCode(bus.getRoute() == null ? null : bus.getRoute().getCode())
                .cameraFacesluice(bus.getCamera() == null ? null : bus.getCamera().getFacesluice())
                .amountOfStudents(bus.getAmountOfStudent() == null ? 0 : bus.getAmountOfStudent())
                .espId(bus.getEspId() == null ? "" : bus.getEspId())
                .busStatus(bus.getStatus() == null ? null : bus.getStatus())
                .cameraFacesluice(bus.getCamera() == null ? null : bus.getCamera().getFacesluice())
                .build();
    }
}
