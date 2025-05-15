package com.fpt.bbusbe.controller;

import com.fpt.bbusbe.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/driver")
@Tag(name = "Driver Controller")
@Slf4j(topic = "DRIVER-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class DriverController {
    private final DriverService driverService;
    

    @Operation(summary = "Get list of a driver", description = "API retrieve drivers from db")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getDriverList(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String sort,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "100000") int size) {
        log.info("Get driver list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "driver list");
        result.put("data", driverService.findAll(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get student list of a driver", description = "API retrieve drivers from db")
    @GetMapping("/available")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getAvailableDriverList(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String sort,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "100000") int size) {
        log.info("Get driver list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "driver list");
        result.put("data", driverService.findAvailableDrivers(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get schedule", description = "API retrieve schedule of a driver from db")
    @GetMapping("/get-schedule")
    public ResponseEntity<Object> getSchedule(@RequestParam(required = true)LocalDate date) {
        log.info("Get schedule");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "schedule list");
        result.put("data", driverService.findScheduleByDate(date));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get driver's schedule by month", description = "API retrieve all schedules of a driver in a specific month from db")
    @GetMapping("/get-schedule-by-month")
    public ResponseEntity<Object> getDriverScheduleByMonth(
            @RequestParam(required = true) Integer year,
            @RequestParam(required = true) Integer month) {

        log.info("Get driver schedule by month");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "driver schedule list for month");
        result.put("data", driverService.findDriverScheduleByMonth(year, month));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
