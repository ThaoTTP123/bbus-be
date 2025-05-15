package com.fpt.bbusbe.controller;


import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointStatusChangeRequest;
import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointCreationRequest;
import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointUpdateRequest;
import com.fpt.bbusbe.model.dto.response.checkpoint.CheckpointResponse;
import com.fpt.bbusbe.service.CheckpointService;
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
@RequestMapping("/checkpoint")
@Tag(name = "Checkpoint Controller")
@Slf4j(topic = "CHECKPOINT-CONTROLLER")
@RequiredArgsConstructor
public class CheckpointController {
    private final CheckpointService checkpointService;

    @Operation(summary = "Get checkpoint list", description = "API retrieve checkpoints from db")
    @GetMapping("/list")
    public ResponseEntity<Object> getList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10000") int size) {
        log.info("Get checkpoint list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "checkpoint list");
        result.put("data", checkpointService.findAll(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get students by checkpoint ID", description = "API retrieve students by checkpoint ID")
    @GetMapping("/students")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getStudentsByCheckpoint(@RequestParam UUID checkpointId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get students by checkpoint");
        result.put("data", checkpointService.getStudentsByCheckpoint(checkpointId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Toggle Checkpoint Status", description = "API đảo trạng thái ACTIVE/INACTIVE của checkpoint")
    @PatchMapping("/{checkpointId}/toggle-status")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> toggleCheckpointStatus(@PathVariable UUID checkpointId) {
        checkpointService.toggleCheckpointStatus(checkpointId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Checkpoint status toggled successfully");

        return ResponseEntity.ok(result);
    }


    @GetMapping("/by-route")
    @Operation(summary = "Get checkpoints by route ID", description = "API retrieve checkpoints by route ID")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN', 'DRIVER', 'ASSISTANT', 'PARENT')")
    public ResponseEntity<Object> getCheckpointsByRoute(@RequestParam UUID routeId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get checkpoints by route");
        result.put("data", checkpointService.getCheckpointsByRoute(routeId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get checkpoints without route", description = "API lấy ra tất cả các điểm đón chưa thuộc bất kỳ tuyến nào")
    @GetMapping("/no-route")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getCheckpointsWithoutRoute() {
        log.info("Get checkpoints without route");

        List<CheckpointResponse> checkpoints = checkpointService.getCheckpointsWithoutRoute();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Lấy danh sách các điểm đón chưa thuộc tuyến thành công");
        result.put("data", checkpoints);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get checkpoints with route", description = "API lấy ra tất cả các điểm đón đã thuộc 1 tuyến")
    @GetMapping("/have-route")
    public ResponseEntity<Object> getCheckpointsWithRoute() {
        log.info("Get checkpoints with route");

        List<CheckpointResponse> checkpoints = checkpointService.getCheckpointsWithRoute();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Lấy ra tất cả các điểm đón đã thuộc 1 tuyến thành công");
        result.put("data", checkpoints);

        return ResponseEntity.ok(result);
    }



    @GetMapping("/count-students")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    @Operation(summary = "Count students in checkpoint", description = "API count all students in checkpoint (regardless of bus)")
    public ResponseEntity<Object> countStudentsInCheckpoint(@RequestParam UUID checkpointId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "count all students in checkpoint (regardless of bus)");
        result.put("data", checkpointService.countStudentsInCheckpoint(checkpointId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @Operation(summary = "Get checkpoint detail", description = "API retrieve checkpoint detail by ID")
    @GetMapping("/{checkpointId}")
    public ResponseEntity<Object> getCheckpointDetail(@PathVariable("checkpointId") UUID id
    ) {
        log.info("Get checkpoint detail by ID: {}", id);

        CheckpointResponse u = checkpointService.findById(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get checkpoint detail");
        result.put("data", u);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Create checkpoint", description = "API add new checkpoint to db")
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> createCheckpoint(@RequestBody @Valid CheckpointCreationRequest checkpointCreateRequest
    ) {
        log.info("Create checkpoint: {}", checkpointCreateRequest);

        CheckpointResponse checkpoint = checkpointService.save(checkpointCreateRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "checkpoint created successfully");
        result.put("data", checkpoint);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Edit checkpoint", description = "API edit checkpoint to db")
    @PutMapping("/upd")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> editCheckpoint(@RequestBody @Valid CheckpointUpdateRequest checkpointUpdateRequest
    ) {
        log.info("Create checkpoint: {}", checkpointUpdateRequest);

        CheckpointResponse checkpoint = checkpointService.update(checkpointUpdateRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "checkpoint updated successfully");
        result.put("data", checkpoint);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Change checkpoint status", description = "API change checkpoint status")
    @PatchMapping("/status")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> changeStatus(@RequestBody @Valid CheckpointStatusChangeRequest checkpointStatusChangeRequest
    ) {
        log.info("Change checkpoint status: {}", checkpointStatusChangeRequest);

        checkpointService.changeStatus(checkpointStatusChangeRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "checkpoint status changed successfully");

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete checkpoint", description = "API delete checkpoint by ID")
    @DeleteMapping("/{checkpointId}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> deleteCheckpoint(@PathVariable("checkpointId") UUID id
    ) {
        log.info("Delete checkpoint by ID: {}", id);

        checkpointService.delete(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "checkpoint deleted successfully");

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Get checkpoint list with amount of student register", description = "API retrieve checkpoints from db with amount of student register")
    @GetMapping("/list-with-registered")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getListWithRegistered(@RequestParam(required = false) String keyword) {
        log.info("Get checkpoint list with amount of student register");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "checkpoint list with amount of student register");
        result.put("data", checkpointService.findAllWithAmountOfStudentRegister(keyword));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get a checkpoint information with list of student register with it and list of bus go through it", description = "API retrieve checkpoint detail information for setting up student")
    @GetMapping("/detailed-with-student-and-bus")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getDetailedWithStudentAndBus(@RequestParam(required = false) UUID checkpointId) {
        log.info("Get checkpoint list with amount of student register");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "checkpoint list with amount of student register");
        result.put("data", checkpointService.getDetailedWithStudentAndBus(checkpointId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get checkpoint list with amount of student", description = "API retrieve checkpoint list with amount of student")
    @GetMapping("/get-an-checkpoint-with-student-count")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getAnCheckpointWithStudentCount(@RequestParam(required = false) String keyword,
                                                                  @RequestParam(required = false) String sort,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10000") int size) {
        log.info("Get checkpoint list with amount of student register");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "checkpoint list with amount of student register");
        result.put("data", checkpointService.getAnCheckpointWithStudentCount(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
