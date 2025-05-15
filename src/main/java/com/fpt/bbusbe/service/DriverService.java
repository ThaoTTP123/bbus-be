package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.dto.response.busSchedule.BusScheduleResponse;
import com.fpt.bbusbe.model.dto.response.driver.DriverPageResponse;
import com.fpt.bbusbe.model.dto.response.parent.ParentPageResponse;

import java.time.LocalDate;
import java.util.List;

public interface DriverService {

    DriverPageResponse findAll(String keyword, String sort, int page, int size);

    List<BusScheduleResponse> findScheduleByDate(LocalDate date);

    DriverPageResponse findAvailableDrivers(String keyword, String sort, int page, int size);

    List<BusScheduleResponse> findDriverScheduleByMonth(int year, int month);
}
