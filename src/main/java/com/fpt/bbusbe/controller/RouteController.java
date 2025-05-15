package com.fpt.bbusbe.controller;

import com.fpt.bbusbe.model.dto.request.route.RouteCreationRequest;
import com.fpt.bbusbe.model.dto.request.route.RouteUpdateRequest;
import com.fpt.bbusbe.model.dto.response.route.RouteResponse;
import com.fpt.bbusbe.model.dto.response.route.RouteResponseUpdate;
import com.fpt.bbusbe.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/route")
@Tag(name = "Route Controller")
@Slf4j(topic = "ROUTE-CONTROLLER")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;

    @Operation(summary = "Get route list", description = "API to retrieve routes from db")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10000") int size) {
        // Implementation for retrieving a paginated list of routes
        return ResponseEntity.ok(routeService.findAll(keyword, sort, page, size));
    }

    @Operation(summary = "Get route detail", description = "API to retrieve route details by ID")
    @GetMapping("/{routeId}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getRouteDetail(@PathVariable("routeId") UUID id) {
        // Implementation for retrieving route details
        return ResponseEntity.ok(routeService.findById(id));
    }

    @GetMapping("/by-bus")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN', 'PARENT')")
    @Operation(summary = "Get route path by bus ID", description = "API to retrieve route path by bus ID")
    public ResponseEntity<Object> getRouteByBusId(@RequestParam UUID busId) {
        String path = routeService.getRoutePathByBusId(busId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get route by bus");
        result.put("data", path);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @Operation(summary = "Create route", description = "API to add a new route to db")
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> createRoute(@RequestBody @Valid RouteCreationRequest routeCreationRequest) {
        // Implementation for creating a new route
        RouteResponse newRoute = routeService.save(routeCreationRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "create route");
        result.put("data", newRoute);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update route info and checkpoints", description = "Update route description and ordered checkpoints")
    @PatchMapping("/update-info-and-checkpoints")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SYSADMIN')")
    public ResponseEntity<Object> updateRouteInfoAndCheckpoints(@RequestBody @Valid RouteUpdateRequest request) {

        log.info("Update route info and checkpoints: {}", request);

        RouteResponseUpdate routeResponse = routeService.updateInfoAndCheckpoints(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Cập nhật thông tin và checkpoint route thành công");
        result.put("data", routeResponse);

        return ResponseEntity.ok(result);
    }




    @Operation(summary = "Delete Route", description = "Xóa route khi các checkpoint của route không có học sinh nào đăng ký")
    @DeleteMapping("/{routeId}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> deleteRoute(@PathVariable UUID routeId) {
        routeService.deleteRoute(routeId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Xóa route thành công");

        return ResponseEntity.ok(result);
    }


}