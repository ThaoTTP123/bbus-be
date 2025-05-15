package com.fpt.bbusbe.controller;


import com.fpt.bbusbe.model.dto.request.bus.BusCreationRequest;
import com.fpt.bbusbe.model.dto.request.bus.BusStatusChangeRequest;
import com.fpt.bbusbe.model.dto.request.bus.BusUpdateRequest;
import com.fpt.bbusbe.model.dto.response.bus.BusResponse;
import com.fpt.bbusbe.service.BusService;
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
@RequestMapping("/bus")
@Tag(name = "Bus Controller")
@Slf4j(topic = "BUS-CONTROLLER")
@RequiredArgsConstructor
public class BusController {
    private final BusService busService;

    @Operation(summary = "Get bus list", description = "API retrieve bus from db")
    @GetMapping("/list")
//    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10000") int size) {
        log.info("Get bus list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "bus list");
        result.put("data", busService.findAll(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get bus by checkpoint ID", description = "API retrieve bus by checkpoint ID")
    @GetMapping("/by-checkpoint")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getBusesByCheckpoint(@RequestParam UUID checkpointId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get buses by checkpoint");
        result.put("data", busService.findBusesByCheckpointId(checkpointId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/by-route")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    @Operation(summary = "Get bus by route ID", description = "API retrieve bus by route ID")
    public ResponseEntity<Object> getBusesByRoute(@RequestParam UUID routeId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get buses by route");
        result.put("data", busService.findBusesByRouteId(routeId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }



    @Operation(summary = "Get bus detail", description = "API retrieve bus detail by ID")
    @GetMapping("/{busId}")
//    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getBusDetail(@PathVariable("busId") UUID id
    ) {
        log.info("Get bus detail by ID: {}", id);

        BusResponse u = busService.findById(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get bus detail");
        result.put("data", u);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Create bus", description = "API add new bus to db")
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> createBus(@RequestBody @Valid BusCreationRequest busCreationRequest
    ) {
        log.info("Create bus: {}", busCreationRequest);

        BusResponse bus = busService.save(busCreationRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "bus created successfully");
        result.put("data", bus);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Edit bus", description = "API edit bus to db")
    @PutMapping("/upd")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> editBus(@RequestBody @Valid BusUpdateRequest busUpdateRequest
    ) {
        log.info("Create bus: {}", busUpdateRequest);

        BusResponse bus = busService.update(busUpdateRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "bus updated successfully");
        result.put("data", bus);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Change bus status", description = "API change bus status")
    @PatchMapping("/status")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> changeStatus(@RequestBody @Valid BusStatusChangeRequest busStatusChangeRequest
    ) {
        log.info("Change bus status: {}", busStatusChangeRequest);

        busService.changeStatus(busStatusChangeRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "bus status changed successfully");

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete bus", description = "API delete bus by ID")
    @DeleteMapping("/{busId}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> deleteBus(@PathVariable("busId") UUID id
    ) {
        log.info("Delete bus by ID: {}", id);

        busService.delete(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "bus deleted successfully");

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Update max capacity for bus", description = "API update max capacity for bus")
    @PostMapping("/upd-max-capacity-for-all-bus")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> updateMaxCapacityForAllBus(@RequestParam int maxCapacity
    ) {
        log.info("Update max capacity for all bus. New max capacity: {}", maxCapacity);

        busService.updateMaxCapacity(maxCapacity);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "max capacity updated successfully");

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }



}
