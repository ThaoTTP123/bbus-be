package com.fpt.bbusbe.controller;

import com.fpt.bbusbe.model.entity.Camera;
import com.fpt.bbusbe.service.CameraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/camera")
@Tag(name = "Camera Controller")
@Slf4j(topic = "CAMERA-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class CameraController {

    @Autowired
    private CameraService cameraService;

    @Operation(summary = "Upload all info", description = "Upload all students' info to all cameras.")
    @PostMapping("/upload-all")
    @PreAuthorize("hasAnyAuthority('sysadmin', 'admin')")
    public ResponseEntity<Object> uploadAll(Pageable pageable) {
        log.info("Upload all students' info to all cameras.");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "bus list");
        result.put("data", "upload successful");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
