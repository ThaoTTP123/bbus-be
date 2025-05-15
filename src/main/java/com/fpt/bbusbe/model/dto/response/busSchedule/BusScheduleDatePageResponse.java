package com.fpt.bbusbe.model.dto.response.busSchedule;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BusScheduleDatePageResponse {
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private List<LocalDate> dates;
}
