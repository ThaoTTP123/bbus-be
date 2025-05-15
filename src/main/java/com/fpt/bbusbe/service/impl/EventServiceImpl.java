package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.event.EventRequest;
import com.fpt.bbusbe.model.dto.response.event.EventResponse;
import com.fpt.bbusbe.model.entity.Event;
import com.fpt.bbusbe.repository.EventRepository;
import com.fpt.bbusbe.service.EventService;
import com.fpt.bbusbe.service.JobSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final JobSchedulerService jobSchedulerService;

    @Override
    public Event findByName(String eventName) {
        return eventRepository.findByName(eventName);
    }


//    @Override
//    public Event save(Event eventCreationRequest) {
//        Event event = eventRepository.findByName(eventCreationRequest.getName());
//        if (event != null) {
//            throw new InvalidDataException("Event already exists");
//        }
//        jobSchedulerService.schedulePickupDeadlineEvent(eventCreationRequest);
//        return eventRepository.save(eventCreationRequest);
//    }
//
//    @Override
//    public void update(Event eventUpdateRequest) {
//        Event event = eventRepository.findByName(eventUpdateRequest.getName());
//        if (event != null) {
//            event.setStart(eventUpdateRequest.getStart() == null ? event.getStart() : eventUpdateRequest.getStart());
//            event.setEnd(eventUpdateRequest.getEnd() == null ? event.getEnd() : eventUpdateRequest.getEnd());
//
//            Event updated = eventRepository.save(event);
//
//            jobSchedulerService.schedulePickupDeadlineEvent(updated); // Reschedule jobs
//        } else {
//            throw new ResourceNotFoundException("Event not found");
//        }
//    }

    @Override
    public Event save(EventRequest request) {
        Event event = eventRepository.findByName(request.getName());
        if (event != null) {
            throw new InvalidDataException("Event already exists");
        }

        Event entity = new Event();
        entity.setName(request.getName());
        entity.setStart(parseWithoutTimezone(request.getStart()));
        entity.setEnd(parseWithoutTimezone(request.getEnd()));

        jobSchedulerService.schedulePickupDeadlineEvent(entity);
        return eventRepository.save(entity);
    }

    @Override
    public void update(EventRequest request) {
        Event event = eventRepository.findByName(request.getName());
        if (event == null) {
            throw new ResourceNotFoundException("Event not found");
        }

        if (request.getStart() != null) {
            event.setStart(parseWithoutTimezone(request.getStart()));
        }

        if (request.getEnd() != null) {
            event.setEnd(parseWithoutTimezone(request.getEnd()));
        }

        Event updated = eventRepository.save(event);
        jobSchedulerService.schedulePickupDeadlineEvent(updated);
    }


    private Date parseWithoutTimezone(String input) {
        // Không gán timezone, giữ nguyên giờ
        LocalDateTime ldt = LocalDateTime.parse(input);
        // Chuyển sang Instant mà KHÔNG gắn offset, lưu đúng giờ
        return Timestamp.valueOf(ldt);
    }


    @Override
    public void deleteByName(String name) {
        Event event = eventRepository.findByName(name);
        if (event != null) {
            eventRepository.delete(event);
        } else {
            throw new ResourceNotFoundException("Event not found");
        }
    }
}
