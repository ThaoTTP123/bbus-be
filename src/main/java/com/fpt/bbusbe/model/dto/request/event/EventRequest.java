package com.fpt.bbusbe.model.dto.request.event;

import lombok.Data;

@Data
public class EventRequest {
    private String name;
    private String start; // "2025-05-05T01:52:00"
    private String end;
}
