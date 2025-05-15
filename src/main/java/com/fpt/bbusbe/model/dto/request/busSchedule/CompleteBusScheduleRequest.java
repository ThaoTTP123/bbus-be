package com.fpt.bbusbe.model.dto.request.busSchedule;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CompleteBusScheduleRequest {
    @NotNull(message = "BusScheduleId là bắt buộc")
    private UUID busScheduleId;

    private String note;
}
