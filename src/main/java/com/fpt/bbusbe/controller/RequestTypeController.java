package com.fpt.bbusbe.controller;


import com.fpt.bbusbe.model.dto.response.requestType.RequestTypeResponse;
import com.fpt.bbusbe.service.RequestTypeService;
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
@RequestMapping("/request-type")
@Tag(name = "RequestType Controller")
@Slf4j(topic = "REQUEST-TYPE-CONTROLLER")
@RequiredArgsConstructor
public class RequestTypeController {
    private final RequestTypeService requestTypeService;

    @Operation(summary = "Get requestType list", description = "API retrieve requestTypes from db")
    @GetMapping("/list")
//    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10000") int size) {
        log.info("Get requestType list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "requestType list");
        result.put("data", requestTypeService.findAll(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get requestType detail", description = "API retrieve requestType detail by ID")
    @GetMapping("/{requestTypeId}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getRequestTypeDetail(@PathVariable("requestTypeId") UUID id
    ) {
        log.info("Get requestType detail by ID: {}", id);

        RequestTypeResponse u = requestTypeService.findById(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get requestType detail");
        result.put("data", u);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

//    @Operation(summary = "Create requestType", description = "API add new requestType to db")
//    @PostMapping("/add")
//    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
//    public ResponseEntity<Object> createRequestType(@RequestBody @Valid RequestTypeCreationRequest requestTypeCreateRequest
//    ) {
//        log.info("Create requestType: {}", requestTypeCreateRequest);
//
//        RequestTypeResponse requestType = requestTypeService.save(requestTypeCreateRequest);
//
//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("status", HttpStatus.CREATED.value());
//        result.put("message", "requestType created successfully");
//        result.put("data", requestType);
//
//        return new ResponseEntity<>(result, HttpStatus.CREATED);
//    }



}
