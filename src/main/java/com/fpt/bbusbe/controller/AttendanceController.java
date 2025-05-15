package com.fpt.bbusbe.controller;

import com.fpt.bbusbe.model.dto.request.attendance.ManualAttendanceRequest;
import com.fpt.bbusbe.model.enums.BusDirection;
import com.fpt.bbusbe.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Slf4j(topic = "ATTENDANCE-CONTROLLER")
@Tag(name = "Attendance Controller")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Get attendance for bus", description = "API attendance for bus")
    @GetMapping("/get-attendance")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN', 'DRIVER', 'ASSISTANT')")
    public ResponseEntity<Object> getList(@RequestParam UUID busId,
                                          @RequestParam LocalDate date,
                                          @RequestParam BusDirection busDirection) {
        log.info("Get attendance for bus");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "bus list");
        result.put("data", attendanceService.findAllByBusIdAndDateAndDirection(busId, date, busDirection));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get attendance of a student", description = "API attendance of a student")
    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getAttendanceHistoryOfAStudent(@PathVariable UUID studentId){
        log.info("Get attendance of a student");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "bus list");
        result.put("data", attendanceService.getAttendanceHistoryOfAStudent(studentId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get attendance of a student of a date for parent", description = "API attendance of a student for parent")
    @GetMapping("/parent/{studentId}")
    @PreAuthorize("hasAnyAuthority('PARENT')")
    public ResponseEntity<Object> getAttendanceOfAStudentForParent(@PathVariable UUID studentId,
                                                                   @RequestParam LocalDate date){
        log.info("Get attendance of a student");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "bus list");
        result.put("data", attendanceService.getAttendanceHistoryOfAStudentForParent(studentId, date));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Manual Attendance", description = "API điểm danh thủ công bởi assistant")
    @PatchMapping("/manual-attendance")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN', 'ASSISTANT', 'DRIVER')")
    public ResponseEntity<Object> manualAttendance(
            @RequestBody @Valid ManualAttendanceRequest req
    ) {
        log.info("Manual attendance request: {}", req);

        attendanceService.manualAttendance(req);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Manual attendance successfully updated");

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Final Report", description = "API final report for Star Primary School")
    @GetMapping("/final-report")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getFinalReport() {
        log.info("Get final report");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Final report");
        result.put("data", attendanceService.generateFinalReport());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Bus Report", description = "API bus report for Star Primary School")
    @GetMapping("/bus-report")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getBusReport() {
        log.info("Get bus report");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Bus report");
        result.put("data", attendanceService.generateBusReport());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Route Report", description = "API route report for Star Primary School")
    @GetMapping("/route-report")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getRouteReport() {
        log.info("Get route report");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Route report");
        result.put("data", attendanceService.generateRouteReport());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Attendance Report", description = "API attendance report for Star Primary School")
    @GetMapping("/attendance-report")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getAttendanceReport() {
        log.info("Get route report");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Route report");
        result.put("data", attendanceService.generateAttendanceReport());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Driver And Assistant Report", description = "API driver and assistant report for Star Primary School")
    @GetMapping("/driver-assistant-report")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getDriverAndAssistantReport() {
        log.info("Get route report");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Route report");
        result.put("data", attendanceService.generateDriverAndAssistantReport());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Dashboard", description = "API for dashboard")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getDashboard() {
        log.info("Get dashboard");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Dashboard");
        result.put("data", attendanceService.dashboard());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
