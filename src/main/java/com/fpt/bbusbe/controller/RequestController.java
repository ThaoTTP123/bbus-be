package com.fpt.bbusbe.controller;


import com.fpt.bbusbe.model.dto.request.request.ReplyRequestRequest;
import com.fpt.bbusbe.model.dto.request.request.RequestCreationRequest;
import com.fpt.bbusbe.model.dto.response.bus.BusWithCheckpointResponse;
import com.fpt.bbusbe.model.dto.response.request.RequestResponse;
import com.fpt.bbusbe.model.entity.Request;
import com.fpt.bbusbe.service.RequestService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/request")
@Tag(name = "Request Controller")
@Slf4j(topic = "CHECKPOINT-CONTROLLER")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @Operation(summary = "Get request list", description = "API retrieve requests from db")
    @GetMapping("/list")
    public ResponseEntity<Object> getList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10000") int size) {
        log.info("Get request list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "request list");
        result.put("data", requestService.findAll(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get request detail", description = "API retrieve request detail by ID")
    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestDetail(@PathVariable("requestId") UUID id
    ) {
        log.info("Get request detail by ID: {}", id);

        RequestResponse u = requestService.findById(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get request detail");
        result.put("data", u);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get my requests", description = "API get list of requests sent by the logged-in user")
    @GetMapping("/my-requests")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN', 'PARENT', 'TEACHER', 'DRIVER', 'ASSISTANT')")
    public ResponseEntity<Object> getMyRequests(
            @RequestParam(required = false) UUID requestTypeId
    ) {
        log.info("Get my requests by requestTypeId: {}", requestTypeId);

        List<RequestResponse> requestResponses = requestService.getMyRequests(requestTypeId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Get my requests successfully");
        result.put("data", requestResponses);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @Operation(summary = "Create request", description = "API add new request to db")
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('PARENT')")
    public ResponseEntity<Object> createRequest(@RequestBody @Valid RequestCreationRequest requestCreationRequest
    ) {
        log.info("Create request: {}", requestCreationRequest);

        RequestResponse requestResponse = requestService.save(requestCreationRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "request created successfully");
        result.put("data", requestResponse);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Reply request", description = "API reply a request to db")
    @PatchMapping("/reply")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN', 'PARENT')")
    public ResponseEntity<Object> replyRequest(@RequestBody @Valid ReplyRequestRequest replyRequestRequest
    ) {
        log.info("Reply request: {}", replyRequestRequest);

        RequestResponse requestResponse = requestService.replyRequest(replyRequestRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "reply request successfully");
        result.put("data", requestResponse);

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }


    @PostMapping("/process-change-checkpoint/{requestId}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    @Operation(summary = "Process change checkpoint request", description = "API process change checkpoint request")
    public ResponseEntity<Object> processChangeCheckpoint(@PathVariable UUID requestId) {
        BusWithCheckpointResponse response = requestService.processChangeCheckpointRequest(requestId);

        if (response == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", 400,
                    "message", "Không có xe đủ chỗ để xử lý đơn"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Đã xử lý đơn đổi điểm đón",
                "data", response
        ));
    }


}
