package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointCreationRequest;
import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointStatusChangeRequest;
import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointUpdateRequest;
import com.fpt.bbusbe.model.dto.response.checkpoint.*;
import com.fpt.bbusbe.model.entity.Checkpoint;
import com.fpt.bbusbe.model.entity.Route;
import com.fpt.bbusbe.model.enums.CheckpointStatus;
import com.fpt.bbusbe.repository.CheckpointRepository;
import com.fpt.bbusbe.repository.RouteRepository;
import com.fpt.bbusbe.repository.StudentRepository;
import com.fpt.bbusbe.service.CheckpointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "CHECKPOINT_SERVICE")
@RequiredArgsConstructor
public class CheckpointServiceImpl implements CheckpointService {

    private final CheckpointRepository checkpointRepository;
    private final StudentRepository studentRepository;
    private final RouteRepository routeRepository;

    @Override
    public CheckpointPageResponse findAll(String keyword, String sort, int page, int size) {
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

        // Nếu không có roleName, truyền `null` để bỏ qua lọc theo role

        Page<Checkpoint> entityPage = checkpointRepository.searchByKeyword(formattedKeyword, pageable);
        return getCheckpointPageResponse(page, size, entityPage);
    }

    @Override
    public List<CheckpointWithStudentResponse> getStudentsByCheckpoint(UUID checkpointId) {
        Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found"));

        return checkpoint.getStudents().stream().map(student -> {
            boolean registered = student.getBus() != null;

            return CheckpointWithStudentResponse.builder()
                    .studentId(student.getId())
                    .studentName(student.getName())
                    .rollNumber(student.getRollNumber())
                    .registered(registered)
                    .busId(registered ? student.getBus().getId() : null)
                    .busName(registered ? student.getBus().getName() : null)
                    .build();
        }).toList();
    }

    @Override
    public int countStudentsInCheckpoint(UUID checkpointId) {
        return studentRepository.countStudentsByCheckpointId(checkpointId);
    }

//    @Override
//    public List<CheckpointResponse> getCheckpointsByRoute(UUID routeId) {
//        List<Checkpoint> checkpoints = checkpointRepository.findAllByRoute_Id(routeId);
//        Checkpoint root = checkpointRepository.findById(UUID.fromString("fdcb7b87-7cf4-4648-820e-b86ca2e4aa88"))
//                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found"));
//
//        checkpoints.add(root);
//
//        return checkpoints.stream().map(cp -> CheckpointResponse.builder()
//                .id(cp.getId())
//                .name(cp.getName())
//                .description(cp.getDescription())
//                .latitude(cp.getLatitude())
//                .longitude(cp.getLongitude())
//                .status(cp.getStatus())
//                .build()
//        ).toList();
//    }

    @Override
    public List<CheckpointWithTimeResponse> getCheckpointsByRoute(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        List<UUID> checkpointIds = Arrays.stream(route.getPath().split("\\s+"))
                .map(String::trim)
                .map(UUID::fromString)
                .toList();

        List<String> checkpointTimes = Arrays.stream(route.getCheckpointTime().split("\\s+"))
                .map(String::trim)
                .toList();

        List<Checkpoint> checkpoints = checkpointRepository.findAllByIdInOrder(checkpointIds.toArray(new UUID[0]));

        Checkpoint root = checkpointRepository.findById(UUID.fromString("fdcb7b87-7cf4-4648-820e-b86ca2e4aa88"))
                .orElseThrow(() -> new ResourceNotFoundException("Root Checkpoint not found"));

        List<CheckpointWithTimeResponse> responses = new ArrayList<>();

        for (int i = 0; i < checkpoints.size(); i++) {
            Checkpoint cp = checkpoints.get(i);
            String time = i < checkpointTimes.size() ? checkpointTimes.get(i) : null;

            responses.add(CheckpointWithTimeResponse.builder()
                    .id(cp.getId())
                    .name(cp.getName())
                    .description(cp.getDescription())
                    .latitude(cp.getLatitude())
                    .longitude(cp.getLongitude())
                    .status(cp.getStatus())
                    .time(time)
                    .build());
        }

        return responses;
    }



    @Override
    public List<CheckpointResponse> getCheckpointsWithoutRoute() {
        List<Checkpoint> checkpoints = checkpointRepository.findAllByRouteIsNull();

        Checkpoint root = checkpointRepository.findById(UUID.fromString("fdcb7b87-7cf4-4648-820e-b86ca2e4aa88"))
                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found"));

        checkpoints.remove(root);

        return checkpoints.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<CheckpointResponse> getCheckpointsWithRoute() {
        List<Checkpoint> checkpoints = checkpointRepository.findAllByRouteIsNotNull();

        return checkpoints.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    private CheckpointResponse convertToResponse(Checkpoint checkpoint) {
        return CheckpointResponse.builder()
                .id(checkpoint.getId())
                .name(checkpoint.getName())
                .description(checkpoint.getDescription())
                .latitude(checkpoint.getLatitude())
                .longitude(checkpoint.getLongitude())
                .status(checkpoint.getStatus())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleCheckpointStatus(UUID checkpointId) {
        Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found"));

        if (checkpointId.toString().equals("fdcb7b87-7cf4-4648-820e-b86ca2e4aa88")) {
            throw new InvalidDataException("Checkpoint gốc không thể chuyển trạng thái");
        }

        // Nếu checkpoint đang ACTIVE → muốn chuyển INACTIVE phải check route trước
        if (checkpoint.getStatus() == CheckpointStatus.ACTIVE) {
            if (checkpoint.getRoute() != null) {
                throw new InvalidDataException("Checkpoint đang thuộc một tuyến (route). Không thể chuyển sang INACTIVE.");
            }
            checkpoint.setStatus(CheckpointStatus.INACTIVE);
        } else {
            // Nếu đang INACTIVE thì cho phép active mà không cần kiểm tra route
            checkpoint.setStatus(CheckpointStatus.ACTIVE);
        }

        checkpointRepository.save(checkpoint);
    }

    @Override
    public CheckpointWithAmountStudentPageResponse getAnCheckpointWithStudentCount(String keyword, String sort, int page, int size) {
        log.info("findAll with amount of student start");

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

        // Nếu không có roleName, truyền `null` để bỏ qua lọc theo role

        Page<CheckpointWithAmountOfStudentResponse> entityPage = checkpointRepository.findAllWithAmountOfStudentRegister(formattedKeyword, pageable);
        return getCheckpointWithAmountStudentPageResponse(page, size, entityPage);
    }

    @Override
    public CheckpointResponse findById(UUID id) {
        Checkpoint checkpoint = getCheckpointEntity(id);
        return CheckpointResponse.builder()
                .id(id)
                .name(checkpoint.getName())
                .description(checkpoint.getDescription())
                .latitude(checkpoint.getLatitude())
                .longitude(checkpoint.getLongitude())
                .status(checkpoint.getStatus())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckpointResponse save(CheckpointCreationRequest req) {
        Checkpoint checkpointByName = checkpointRepository.findByName(req.getCheckpointName());
        if (checkpointByName != null) {
            throw new IllegalArgumentException("Checkpoint name is already existed");
        }

        Checkpoint checkpoint = Checkpoint.builder()
                .name(req.getCheckpointName())
                .description(req.getDescription())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .status(CheckpointStatus.ACTIVE)
                .build();

        checkpoint = checkpointRepository.save(checkpoint);
        return getCheckpointResponse(checkpoint);
    }

    @Override
    public CheckpointResponse update(CheckpointUpdateRequest req) {
        UUID id = req.getId();

        Checkpoint checkpoint = checkpointRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found"));

        checkpoint.setName(req.getCheckpointName());
        checkpoint.setDescription(req.getDescription());
        checkpoint.setLatitude(req.getLatitude());
        checkpoint.setLongitude(req.getLongitude());

        checkpoint = checkpointRepository.save(checkpoint);
        return getCheckpointResponse(checkpoint);
    }

    @Override
    public void changeStatus(CheckpointStatusChangeRequest req) {
        Checkpoint checkpoint = checkpointRepository.findById(req.getId()).orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found"));
        checkpoint.setStatus(req.getStatus());
        checkpointRepository.save(checkpoint);
    }

    @Override
    public void delete(UUID id) {
        checkpointRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found"));
        checkpointRepository.deleteById(id);
    }

    @Override
    public Checkpoint findByName(String name) {
        return null;
    }

    @Override
    public List<CheckpointWithRegisteredResponse> findAllWithAmountOfStudentRegister(String keyword) {
        log.info("findAll with register start");

        // Chuẩn hóa `keyword` (nếu có) để tránh lỗi query
        String formattedKeyword = (StringUtils.hasLength(keyword)) ? "%" + keyword.toLowerCase() + "%" : "%";
        List<Object[]> results = checkpointRepository.findAllWithAmountOfStudentRegister(formattedKeyword);
        return results.stream().map(obj -> new CheckpointWithRegisteredResponse(
                (UUID) obj[0]  ,   // id
                (String) obj[1],                // name
                ((Number) obj[2]).intValue(),   // pending
                ((Number) obj[3]).intValue()    // registered
        )).collect(Collectors.toList());
    }

    @Override
    public CheckpointWithStudentAndBus getDetailedWithStudentAndBus(UUID checkpointId) {
        log.info("Get checkpoint detail with list of student and list of bus start");

        Checkpoint checkpoint = checkpointRepository.findById(checkpointId).orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found"));
        @SuppressWarnings("unused")
        CheckpointWithStudentAndBus checkpointWithStudentAndBus = CheckpointWithStudentAndBus.builder()
                .checkpointId(checkpointId)
                .checkpointName(checkpoint.getName())
                .status(checkpoint.getStatus())
                .buses(checkpoint.getRoute().getBuses().stream().map(
                        bus -> CheckpointWithStudentAndBus.Bus.builder()
                                .busId(bus.getId())
                                .busName(bus.getName())
                                .licensePlate(bus.getLicensePlate())
                                .status(bus.getStatus())
                                .build()
                ).toList())
                .students(checkpoint.getStudents().stream().map(
                        student -> CheckpointWithStudentAndBus.Student.builder()
                                .studentId(student.getId())
                                .studentName(student.getName())
                                .rollNumber(student.getRollNumber())
                                .bus(student.getBus() != null ? student.getBus().getId() : null)
                                .build()
                ).toList())
                .build();
        return null;
    }

    private Checkpoint getCheckpointEntity(UUID id) {
        return checkpointRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found"));
    }

    private static CheckpointPageResponse getCheckpointPageResponse(int page, int size, Page<Checkpoint> checkpointEntities) {
        log.info("Convert Checkpoint Entity Page");

        List<CheckpointResponse> checkpointList = checkpointEntities.stream().map(entity -> CheckpointResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .status(entity.getStatus())
                .build()
        ).toList();

        CheckpointPageResponse response = new CheckpointPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(checkpointEntities.getTotalElements());
        response.setTotalPages(checkpointEntities.getTotalPages());
        response.setCheckpoints(checkpointList);

        return response;
    }

    private static CheckpointWithAmountStudentPageResponse getCheckpointWithAmountStudentPageResponse(int page, int size, Page<CheckpointWithAmountOfStudentResponse> checkpointEntities) {
        log.info("Convert Checkpoint Entity Page");

        List<CheckpointWithAmountOfStudentResponse> checkpointList = checkpointEntities.stream().map(entity -> CheckpointWithAmountOfStudentResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .status(entity.getStatus())
                .amountOfStudent(entity.getAmountOfStudent())
                .build()
        ).toList();

        CheckpointWithAmountStudentPageResponse response = new CheckpointWithAmountStudentPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(checkpointEntities.getTotalElements());
        response.setTotalPages(checkpointEntities.getTotalPages());
        response.setCheckpoints(checkpointList);

        return response;
    }

    private static CheckpointResponse getCheckpointResponse(Checkpoint checkpoint) {
        return CheckpointResponse.builder()
                .id(checkpoint.getId())
                .name(checkpoint.getName())
                .description(checkpoint.getDescription())
                .latitude(checkpoint.getLatitude())
                .longitude(checkpoint.getLongitude())
                .status(checkpoint.getStatus())
                .build();
    }
}
