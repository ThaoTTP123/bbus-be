package com.fpt.bbusbe.model.dto.request.busSchedule;

import com.fpt.bbusbe.model.enums.BusStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Setter
@Getter
@ToString
public class BusScheduleStatusChangeRequest {
    private UUID id;
    private BusStatus status;
}
