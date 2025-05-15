package com.fpt.bbusbe.controller;

import com.fpt.bbusbe.service.AssistantService;
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
@RequestMapping("/assistant")
@Tag(name = "Assistant Controller")
@Slf4j(topic = "DRIVER-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class AssistantController {
    private final AssistantService assistantService;
    

    @Operation(summary = "Get all assistant", description = "API retrieve assistants from db")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getAssistantList(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String sort,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "100000") int size) {
        log.info("Get user list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "assistant list");
        result.put("data", assistantService.findAll(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get list of a available assistant", description = "API retrieve assistants from db")
    @GetMapping("/available")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getAvailableAssistantList(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String sort,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "100000") int size) {
        log.info("Get assistant list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "assistant list");
        result.put("data", assistantService.findAvailableAssistants(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get schedule", description = "API retrieve schedule of a assistant from db")
    @GetMapping("/get-schedule")
    public ResponseEntity<Object> getSchedule(@RequestParam(required = true)LocalDate date) {
        log.info("Get schedule");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "bus list");
        result.put("data", assistantService.findScheduleByDate(date));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @Operation(summary = "Get schedule by month", description = "API retrieve all schedules of an assistant in a specific month from db")
    @GetMapping("/get-schedule-by-month")
    public ResponseEntity<Object> getScheduleByMonth(
            @RequestParam(required = true) Integer year,
            @RequestParam(required = true) Integer month) {

        log.info("Get schedule by month");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "bus schedule list for month");
        result.put("data", assistantService.findScheduleByMonth(year, month));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
