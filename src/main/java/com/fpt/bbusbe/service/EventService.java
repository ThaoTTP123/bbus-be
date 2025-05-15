package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.dto.request.event.EventRequest;
import com.fpt.bbusbe.model.dto.response.event.EventResponse;
import com.fpt.bbusbe.model.entity.Event;
import jakarta.validation.Valid;

public interface EventService {
    Event findByName(String eventName);

    Event save(EventRequest request);

    void update(EventRequest request);

//    Event save(@Valid Event eventCreationRequest);
//
//    void update(@Valid Event eventUpdateRequest);

    void deleteByName(String name);
}
