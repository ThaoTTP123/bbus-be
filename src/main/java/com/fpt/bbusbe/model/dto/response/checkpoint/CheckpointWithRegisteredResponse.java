package com.fpt.bbusbe.model.dto.response.checkpoint;


import lombok.*;

import java.util.UUID;

@Data
@Builder
@Getter
@Setter
public class CheckpointWithRegisteredResponse {
    private UUID id;
    private String name;
    private int registered;
    private int pending;

    public CheckpointWithRegisteredResponse(UUID id, String name, int pending, int registered) {
        this.id = id;
        this.name = name;
        this.registered = registered;
        this.pending = pending;
    }
}
