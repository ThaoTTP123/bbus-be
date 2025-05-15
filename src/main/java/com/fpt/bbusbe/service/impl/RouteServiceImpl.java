package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.route.RouteUpdateRequest;
import com.fpt.bbusbe.model.dto.response.route.RouteResponseUpdate;
import com.fpt.bbusbe.model.entity.Bus;
import com.fpt.bbusbe.model.entity.Checkpoint;
import com.fpt.bbusbe.model.entity.Route;
import com.fpt.bbusbe.model.dto.request.route.RouteCreationRequest;
import com.fpt.bbusbe.model.dto.response.route.RoutePageResponse;
import com.fpt.bbusbe.model.dto.response.route.RouteResponse;
import com.fpt.bbusbe.repository.BusRepository;
import com.fpt.bbusbe.repository.CheckpointRepository;
import com.fpt.bbusbe.repository.RouteRepository;
import com.fpt.bbusbe.repository.StudentRepository;
import com.fpt.bbusbe.service.RouteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j(topic = "ROUTE-SERVICE")
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final CheckpointRepository checkpointRepository;
    private final StudentRepository studentRepository;

    @Override
    public long countTotalRoutes() {
        return routeRepository.count();
    }

    @Override
    public RoutePageResponse findAll(String keyword, String sort, int page, int size) {
        log.info("Fetching route list with keyword: {}, sort: {}, page: {}, size: {}", keyword, sort, page, size);
        PageRequest pageRequest = StringUtils.hasText(sort)
                ? PageRequest.of(page, size, Sort.by(sort))
                : PageRequest.of(page, size);

        Page<Route> routeEntities = StringUtils.hasText(keyword)
                ? routeRepository.findByKeyword(keyword, pageRequest)
                : routeRepository.findAll(pageRequest);

        return getRoutePageResponse(page, size, routeEntities);
    }

    @Override
    public String getRoutePathByBusId(UUID busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        if (bus.getRoute() == null) {
            throw new ResourceNotFoundException("Bus has no route assigned");
        }

        return bus.getRoute().getPath();
    }


    @Override
    public RouteResponse findById(UUID id) {
        log.info("Fetching route by ID: {}", id);
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route with this ID not found: " + id));
        return getRouteResponse(route);
    }

    private static boolean isSortedAscending(List<String> times) {
        List<LocalTime> sorted = times.stream()
                .map(LocalTime::parse)
                .sorted()
                .toList();
        return !sorted.equals(times.stream().map(LocalTime::parse).toList());
    }

    private static boolean isSortedDescending(List<String> times) {
        List<LocalTime> sorted = times.stream()
                .map(LocalTime::parse)
                .sorted(Comparator.reverseOrder())
                .toList();
        return !sorted.equals(times.stream().map(LocalTime::parse).toList());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public RouteResponse save(RouteCreationRequest req) {
        log.info("Creating new route with data: {}", req);

        String fixedCheckpointId = "fdcb7b87-7cf4-4648-820e-b86ca2e4aa88";
        String fixedCheckpointTime = "07:00/16:30";

        Set<String> requestCheckpointIds = new HashSet<>(Arrays.asList(req.getPath().trim().split("\\s+")));

        List<String> requestCheckpointStartTimes = new ArrayList<>();
        List<String> requestCheckpointEndTimes = new ArrayList<>();

        for (String timePair : req.getCheckpointTime().split(" ")) {
            try {
                String[] parts = timePair.split("/");
                if (parts.length == 2) {
                    requestCheckpointStartTimes.add(parts[0]);
                    requestCheckpointEndTimes.add(parts[1]);
                }
            } catch (Exception e) {
                throw new InvalidDataException("Thời gian không hợp lệ: " + timePair);
            }
        }

        if(isSortedAscending(requestCheckpointStartTimes)) {
            throw new InvalidDataException("Thời gian bắt đầu không được sắp xếp tăng dần.");
        }
        if(isSortedDescending(requestCheckpointEndTimes)) {
            throw new InvalidDataException("Thời gian kết thúc không được sắp xếp giảm dần.");
        }
        if (requestCheckpointIds.size() != requestCheckpointStartTimes.size()) {
            throw new InvalidDataException("Số lượng checkpoint và thời gian không khớp nhau.");
        }
        // Check trùng route dựa vào tập hợp checkpoint (không tính checkpoint cố định)
        Optional<Route> existingRoute = findRouteByCheckpointSet(requestCheckpointIds);
        if (existingRoute.isPresent()) {
            throw new RuntimeException("Tuyến này đã được tạo và có tên là " + existingRoute.get().getCode());
        }

        // Lấy code mới nhất và tự động tăng code
        String lastCode = routeRepository.findAll(Sort.by(Sort.Direction.DESC, "code"))
                .stream()
                .findFirst()
                .map(Route::getCode)
                .orElse("R000");
        String newCode = String.format("R%03d", Integer.parseInt(lastCode.substring(1)) + 1);

        // Tạo route mới
        Route route = new Route();
        route.setCode(newCode);
        route.setDescription(req.getDescription());

        // Gắn thêm checkpoint cố định vào path để lưu DB
        route.setPath(req.getPath().trim() + " " + fixedCheckpointId);
        route.setCheckpointTime(req.getCheckpointTime().trim() + " " + fixedCheckpointTime);

        route.setPeriodStart(req.getPeriodStart());
        route.setPeriodEnd(req.getPeriodEnd());

        routeRepository.saveAndFlush(route);

        // Cập nhật lại route_id cho các checkpoint mới
        List<UUID> checkpointIds = requestCheckpointIds.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        checkpointRepository.updateRouteForCheckpoints(route.getId(), checkpointIds, UUID.fromString(fixedCheckpointId));

        return getRouteResponse(route);
    }


    private Optional<Route> findRouteByCheckpointSet(Set<String> checkpointIdsWithoutFixedCheckpoint) {
        List<Route> routes = routeRepository.findAllRoutes();

        String fixedCheckpointId = "fdcb7b87-7cf4-4648-820e-b86ca2e4aa88";

        return routes.stream().filter(route -> {
            Set<String> routeCheckpointIds = new HashSet<>(Arrays.asList(route.getPath().split("\\s+")));
            routeCheckpointIds.remove(fixedCheckpointId); // luôn bỏ checkpoint cố định ra khi so sánh
            return routeCheckpointIds.equals(checkpointIdsWithoutFixedCheckpoint);
        }).findFirst();
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public RouteResponseUpdate updateInfoAndCheckpoints(RouteUpdateRequest request) {
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy route với ID này."));

        Checkpoint root = checkpointRepository.findById(UUID.fromString("fdcb7b87-7cf4-4648-820e-b86ca2e4aa88"))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy checkpoint gốc với ID này."));

        // Danh sách checkpoint hiện tại của route
        List<Checkpoint> currentCheckpoints = checkpointRepository.findAllByRoute_Id(route.getId());
        currentCheckpoints.add(root);
        Set<UUID> currentCheckpointIds = currentCheckpoints.stream()
                .map(Checkpoint::getId).collect(Collectors.toSet());

        // Danh sách checkpoint mới truyền vào
        List<UUID> newCheckpointIds = request.getOrderedCheckpointIds();
        List<Checkpoint> newCheckpoints = checkpointRepository.findAllById(newCheckpointIds);

        if (newCheckpoints.size() != newCheckpointIds.size()) {
            throw new InvalidDataException("Có checkpoint không tồn tại trong danh sách gửi lên.");
        }

        // ✅ Check checkpoint mới thêm vào không thuộc route nào khác
        newCheckpoints.stream()
                .filter(cp -> cp.getRoute() != null && !cp.getRoute().getId().equals(route.getId()))
                .findAny()
                .ifPresent(cp -> {
                    throw new InvalidDataException("Checkpoint [" + cp.getName() + "] đã thuộc route khác.");
                });

        // ✅ Checkpoint bị xóa khỏi route (tồn tại trong current nhưng không tồn tại trong new)
        Set<UUID> checkpointsToRemove = currentCheckpointIds.stream()
                .filter(id -> !newCheckpointIds.contains(id))
                .collect(Collectors.toSet());

        for (UUID checkpointId : checkpointsToRemove) {
            Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                    .orElseThrow(() -> new ResourceNotFoundException("Checkpoint không tồn tại."));

            boolean hasStudent = studentRepository.existsByCheckpoint_Id(checkpointId);
            if (hasStudent) {
                throw new InvalidDataException("Không thể xóa checkpoint [" + checkpoint.getName() + "] vì đang có học sinh đăng ký.");
            }

            checkpoint.setRoute(null);
            checkpointRepository.save(checkpoint);
        }

        // ✅ Thêm checkpoint mới vào route (tồn tại trong new nhưng không tồn tại trong current)
        Set<UUID> checkpointsToAdd = newCheckpointIds.stream()
                .filter(id -> !currentCheckpointIds.contains(id))
                .collect(Collectors.toSet());

        for (UUID checkpointId : checkpointsToAdd) {
            Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                    .orElseThrow(() -> new ResourceNotFoundException("Checkpoint không tồn tại."));

            checkpoint.setRoute(route);
            checkpointRepository.save(checkpoint);
        }

        // ✅ Xem số lượng checkpoint mới và số lượng checkpoint time có giống nhau không
        if (request.getOrderedCheckpointTimes() != null
                && request.getOrderedCheckpointIds().size() != request.getOrderedCheckpointTimes().size()) {
            throw new InvalidDataException("Số lượng checkpoint và thời gian không khớp nhau.");
        }

        // ✅ Update lại trường path đúng thứ tự mới
        route.setPath(newCheckpointIds.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(" ")));

        // ✅ Update description nếu có thay đổi
        if (request.getDescription() != null) {
            route.setDescription(request.getDescription());
        }

        List<String> orderedCheckpointTimeStarts = new ArrayList<>();
        List<String> orderedCheckpointTimeEnds = new ArrayList<>();
        for (String timePair : request.getOrderedCheckpointTimes()) {
            try {
                String[] parts = timePair.split("/");
                if (parts.length == 2) {
                    orderedCheckpointTimeStarts.add(parts[0]);
                    orderedCheckpointTimeEnds.add(parts[1]);
                }
            } catch (Exception e) {
                throw new InvalidDataException("Thời gian không hợp lệ: " + timePair);
            }
        }

        if(isSortedAscending(orderedCheckpointTimeStarts)) {
            throw new InvalidDataException("Thời gian bắt đầu không được sắp xếp tăng dần.");
        }
        if(isSortedDescending(orderedCheckpointTimeEnds)) {
            throw new InvalidDataException("Thời gian kết thúc không được sắp xếp giảm dần.");
        }

        // ✅ Update checkpoint time nếu có thay đổi
        route.setCheckpointTime(String.join(" ", request.getOrderedCheckpointTimes()));

        routeRepository.save(route);

        return getRouteResponseUpdate(route);
    }

    private RouteResponseUpdate getRouteResponseUpdate(Route route) {
        List<UUID> checkpointIdsInPathOrder = Arrays.stream(route.getPath().split(" "))
                .map(UUID::fromString).toList();

        Map<UUID, Checkpoint> checkpointMap = checkpointRepository.findAllById(checkpointIdsInPathOrder).stream()
                .collect(Collectors.toMap(Checkpoint::getId, cp -> cp));

        return RouteResponseUpdate.builder()
                .id(route.getId())
                .code(route.getCode())
                .description(route.getDescription())
                .checkpoints(checkpointIdsInPathOrder.stream()
                        .map(id -> {
                            Checkpoint cp = checkpointMap.get(id);
                            return RouteResponseUpdate.CheckpointResponse.builder()
                                    .id(cp.getId())
                                    .name(cp.getName())
                                    .latitude(cp.getLatitude())
                                    .longitude(cp.getLongitude())
                                    .description(cp.getDescription())
                                    .status(cp.getStatus())
                                    .build();
                        }).collect(Collectors.toList()))
                .orderedCheckpointTimes(Arrays.stream(route.getCheckpointTime().split(" ")).toList())
                .build();
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteRoute(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route không tồn tại"));

        // Lấy tất cả checkpoint thuộc route này
        List<Checkpoint> checkpoints = checkpointRepository.findAllByRoute_Id(routeId);

        // Kiểm tra từng checkpoint xem có học sinh nào đăng ký không
        for (Checkpoint checkpoint : checkpoints) {
            long studentCount = studentRepository.countByCheckpoint_Id(checkpoint.getId());
            if (studentCount > 0) {
                throw new InvalidDataException(
                        "Không thể xóa route vì checkpoint [" + checkpoint.getName() + "] đang có học sinh đăng ký."
                );
            }
        }

        // Gỡ route ra khỏi tất cả checkpoint (không xóa checkpoint)
        for (Checkpoint checkpoint : checkpoints) {
            checkpoint.setRoute(null);
            checkpointRepository.save(checkpoint);
        }

        // Sau đó xóa route
        routeRepository.delete(route);
    }


    private static RoutePageResponse getRoutePageResponse(int page, int size, Page<Route> routeEntities) {
        log.info("Convert Bus Entity Page");

        List<RouteResponse> routeResponses = routeEntities.stream().map(RouteServiceImpl::getRouteResponse).toList();

        RoutePageResponse response = new RoutePageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(routeEntities.getTotalElements());
        response.setTotalPages(routeEntities.getTotalPages());
        response.setRoutes(routeResponses);

        return response;
    }

    private static RouteResponse getRouteResponse(Route route) {
        RouteResponse response = new RouteResponse();
        response.setId(route.getId());
        response.setCode(route.getCode());
        response.setDescription(route.getDescription());
        response.setPath(route.getPath());
        response.setCheckpointTime(route.getCheckpointTime());
        response.setPeriodStart(route.getPeriodStart());
        response.setPeriodEnd(route.getPeriodEnd());
        response.setCreatedAt(route.getCreatedAt() != null ? route.getCreatedAt() : null);
        response.setUpdatedAt(route.getUpdatedAt() != null ? route.getUpdatedAt() : null);
        return response;
    }
}