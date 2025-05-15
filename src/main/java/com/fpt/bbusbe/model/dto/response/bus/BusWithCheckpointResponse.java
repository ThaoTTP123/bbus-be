package com.fpt.bbusbe.model.dto.response.bus;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class BusWithCheckpointResponse {
    private UUID busId;
    private String busName;
    private String licensePlate;
    private int maxCapacity;
    private int amountOfStudent;

    private UUID checkpointId;
    private String checkpointName;
    private String checkpointDescription;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
