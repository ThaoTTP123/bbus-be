package com.fpt.bbusbe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.bbusbe.service.CameraRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/camera-request")
@Tag(name = "CameraRequest Controller")
@Slf4j(topic = "CAMERA_REQUEST-CONTROLLER")
@RequiredArgsConstructor
public class CameraRequestController {
    private final CameraRequestService cameraRequestService;

    @Operation(summary = "Get cameraRequest list", description = "API to retrieve cameraRequests from db")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10000") int size) {
        // Implementation for retrieving a paginated list of cameraRequests
        return ResponseEntity.ok(cameraRequestService.findAll(keyword, sort, page, size));
    }

    @Operation(summary = "Get cameraRequest detail", description = "API to retrieve cameraRequest details by ID")
    @GetMapping("/{cameraRequestId}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getCameraRequestDetail(@PathVariable("cameraRequestId") UUID id) {
        // Implementation for retrieving cameraRequest details
        return ResponseEntity.ok(cameraRequestService.findById(id));
    }

    @Operation(summary = "Create cameraRequest", description = "API to add a new cameraRequest to db")
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> createCameraRequest(@RequestBody @Valid UUID cameraRequestId) {
        // Implementation for creating a new cameraRequest
        cameraRequestService.save(cameraRequestId);
        return ResponseEntity.status(HttpStatus.CREATED).body("CameraRequest created successfully");
    }

    @Operation(summary = "Upload all unsuccessful cameraRequestDetails", description = "API to upload all unsuccessful cameraRequestDetails to camera through MQTT")
    @GetMapping("/upload-all-unsuccessful")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> uploadAllUnsuccessfulCameraRequestDetails() throws JsonProcessingException {
        // Implementation for uploading all unsuccessful cameraRequestDetails
        cameraRequestService.uploadAllUnsuccessfulCameraRequestDetails();
        return ResponseEntity.ok("All unsuccessful cameraRequestDetails uploaded successfully");
    }
}
