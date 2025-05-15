package com.fpt.bbusbe.controller;

import com.fpt.bbusbe.model.dto.request.busSchedule.BusScheduleAssignRequest;
import com.fpt.bbusbe.model.dto.request.busSchedule.CompleteBusScheduleRequest;
import com.fpt.bbusbe.service.BusScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/bus-schedule")
@Tag(name = "Bus Schedule Controller")
@Slf4j(topic = "BUS-SCHEDULE-CONTROLLER")
@RequiredArgsConstructor
public class BusScheduleController {

    private final BusScheduleService busScheduleService;

    @Operation(summary = "Get bus-schedule list", description = "API retrieve bus-schedule from db")
    @GetMapping("/list")
//    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10000") int size) {
        log.info("Get bus-schedule list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "bus-schedule list");
        result.put("data", busScheduleService.findAll(page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get bus-schedule by month", description = "API retrieve bus-schedule by month from db")
    @GetMapping("/by-month")
//    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getBusScheduleByMonth(@RequestParam String month,
                                                        @RequestParam(required = false) String sort,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10000") int size) {

        log.info("Get bus-schedule list by month: {}", month);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "bus-schedule list");
        result.put("data", busScheduleService.findByMonth(month, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get bus-schedule dates by month", description = "API retrieve bus-schedule dates by month from db")
    @GetMapping("/dates")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getBusScheduleDatesByMonth(@RequestParam String month,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(defaultValue = "asc") String sort) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get scheduled dates by month");
        result.put("data", busScheduleService.findDatesByMonth(month, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }



    @Operation(summary = "Assign bus-schedule", description = "API assign bus-schedule")
    @PostMapping("/assign-batch")
//    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> assignBusScheduleBatch(@RequestBody @Valid BusScheduleAssignRequest request) {
        log.info("Assign bus-schedule");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "bus-schedule list");
        result.put("data", busScheduleService.assignSchedulesForAllBusesOnDates(request.getDates()));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping()
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> deleteSchedulesByDate(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        busScheduleService.deleteAllSchedulesByDate(date);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Deleted all schedules and attendance on " + date);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Complete Bus Schedule", description = "API kết thúc chuyến xe bus trong ngày")
    @PostMapping("/complete")
    @PreAuthorize("hasAnyAuthority('ASSISTANT', 'DRIVER', 'ADMIN', 'SYSADMIN')")
    public ResponseEntity<Object> completeBusSchedule(@RequestBody @Valid CompleteBusScheduleRequest req) {
        log.info("Request to complete bus schedule: {}", req);

        busScheduleService.completeBusSchedule(req);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Bus schedule completed successfully");

        return ResponseEntity.ok(result);
    }


}
