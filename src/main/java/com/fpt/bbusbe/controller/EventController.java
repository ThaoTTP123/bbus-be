package com.fpt.bbusbe.controller;

import com.fpt.bbusbe.model.dto.request.event.EventRequest;
import com.fpt.bbusbe.model.entity.Event;
import com.fpt.bbusbe.service.EventService;
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

@RestController
@RequestMapping("/event")
@Tag(name = "Event Controller")
@Slf4j(topic = "EVENT-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class EventController {
    private final EventService eventService;

    @Operation(summary = "Get event detail", description = "API to retrieve event details by Name")
    @GetMapping("/{eventName}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN','PARENT')")
    public ResponseEntity<Object> getEventDetail(@PathVariable("eventName") String eventName) {
        // Implementation for retrieving event details
        return ResponseEntity.ok(eventService.findByName(eventName));
    }

    @Operation(summary = "Create event", description = "API to add a new event to db")
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> createEvent(@RequestBody @Valid EventRequest eventCreationRequest) {
        // Implementation for creating a new event
        Event newEvent = eventService.save(eventCreationRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "create event");
        result.put("data", newEvent);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Edit event", description = "API to edit an existing event")
    @PutMapping("/upd")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> editEvent(@RequestBody @Valid EventRequest eventUpdateRequest) {
        // Implementation for editing an existing event
        eventService.update(eventUpdateRequest);
        return ResponseEntity.ok("Event updated successfully");
    }

    @Operation(summary = "Delete event", description = "API to delete a event by ID")
    @DeleteMapping("/{eventName}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> deleteEvent(@PathVariable("eventName") String name) {
        // Implementation for deleting a event
        eventService.deleteByName(name);
        return ResponseEntity.ok("Event deleted successfully");
    }
}
