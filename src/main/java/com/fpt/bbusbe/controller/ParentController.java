package com.fpt.bbusbe.controller;

import com.fpt.bbusbe.model.dto.request.student.StudentUpdateByParentRequest;
import com.fpt.bbusbe.service.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/parent")
@Tag(name = "Parent Controller")
@Slf4j(topic = "PARENT-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class ParentController {

    private final ParentService parentService;

    @Operation(summary = "Get student list of a parent", description = "API retrieve students of a parent from db")
    @GetMapping("/list-student")
    @PreAuthorize("hasAnyAuthority('PARENT')")
    public ResponseEntity<Object> getStudentListOfAParent(){

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "student list");
        result.put("data", parentService.findStudentsOfAParent());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get student list of a parent", description = "API retrieve students of a parent from db")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getParentList(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String sort,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "100000") int size) {
        log.info("Get user list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "user list");
        result.put("data", parentService.findAll(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Register checkpoint", description = "API for parent to register checkpoint the first time for the student")
    @PostMapping("/register-checkpoint")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'PARENT')")
    public ResponseEntity<Object> registerCheckpoint(@RequestParam UUID studentId,
                                                     @RequestParam UUID checkpointId) {
        log.info("Register checkpoint for student: {}", studentId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "register checkpoint success");
        result.put("data", parentService.registerCheckpoint(studentId, checkpointId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Register checkpoint", description = "API for parent to register checkpoint the first time for the students")
    @PostMapping("/register-checkpoint-for-all-children")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'PARENT')")
    public ResponseEntity<Object> registerCheckpointForAllChildren(@RequestParam UUID checkpointId) {
        log.info("Register checkpoint for all children of parent");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "register checkpoint success");
        result.put("data", parentService.registerCheckpointForAllChildren(checkpointId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Change checkpoint", description = "API for parent to change checkpoint the first time for the student")
    @PatchMapping("/checkpoint/register/one")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'PARENT')")
    public ResponseEntity<Object> upsertCheckpoint(@RequestParam UUID studentId,
                                                     @RequestParam UUID checkpointId) {
        log.info("Register checkpoint for student: {}", studentId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "register checkpoint success");
        result.put("data", parentService.upsertCheckpoint(studentId, checkpointId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Change checkpoint", description = "API for parent to change checkpoint the first time for the students")
    @PatchMapping("/checkpoint/register/all")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'PARENT')")
    public ResponseEntity<Object> upsertCheckpointForAllChildren(@RequestParam UUID checkpointId) {
        log.info("Register checkpoint for all children of parent");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "register checkpoint success");
        result.put("data", parentService.upsertCheckpointForAll(checkpointId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Update student's basic info by parent", description = "Phụ huynh cập nhật thông tin cơ bản cho con")
    @PatchMapping("/update-student-info")
    @PreAuthorize("hasAuthority('PARENT')")
    public ResponseEntity<Object> updateStudentInfo(@RequestBody @Valid StudentUpdateByParentRequest request) {
        parentService.updateStudentInfo(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Student info updated successfully");

        return ResponseEntity.ok(result);
    }


}