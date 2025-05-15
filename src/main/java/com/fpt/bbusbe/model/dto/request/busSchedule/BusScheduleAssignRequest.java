package com.fpt.bbusbe.model.dto.request.busSchedule;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BusScheduleAssignRequest {
    private List<LocalDate> dates;
}
