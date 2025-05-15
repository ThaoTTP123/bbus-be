package com.fpt.bbusbe.controller;

import com.fpt.bbusbe.service.RequestService;
import com.fpt.bbusbe.service.RouteService;
import com.fpt.bbusbe.service.StudentService;
import com.fpt.bbusbe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard Controller")
@Slf4j(topic = "DASHBOARD-CONTROLLER")
@RequiredArgsConstructor
public class DashboardController {

    private final StudentService studentService;
    private final RequestService requestService;
    private final UserService userService;
    private final RouteService routeService;

    @GetMapping("/count-total-student")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    @Operation(summary = "Count total students", description = "API to count the total number of students in the system")
    public ResponseEntity<Object> countTotalStudents() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "count total students");
        result.put("data", studentService.countTotalStudents());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/request-stats")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    @Operation(summary = "Get request statistics", description = "API to get the number of pending requests and total requests")
    public ResponseEntity<Object> getRequestStats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Request statistics");
        result.put("data", Map.of(
                "pendingRequests", requestService.countPendingRequests(),
                "totalRequests", requestService.countTotalRequests()
        ));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/user-stats")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    @Operation(summary = "Get user account statistics", description = "API to get total user accounts, active accounts, and inactive accounts")
    public ResponseEntity<Object> getUserStats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "User account statistics");
        result.put("data", Map.of(
                "totalUsers", userService.countTotalUsers(),
                "activeUsers", userService.countActiveUsers(),
                "inactiveUsers", userService.countInactiveUsers()
        ));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/count-total-routes")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    @Operation(summary = "Count total bus routes", description = "API to count the total number of bus routes in the system")
    public ResponseEntity<Object> countTotalBusRoutes() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Count total bus routes");
        result.put("data", routeService.countTotalRoutes());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
