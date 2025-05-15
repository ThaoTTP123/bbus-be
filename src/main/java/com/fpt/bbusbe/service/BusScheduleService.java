package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.dto.request.busSchedule.BusScheduleCreationRequest;
import com.fpt.bbusbe.model.dto.request.busSchedule.BusScheduleStatusChangeRequest;
import com.fpt.bbusbe.model.dto.request.busSchedule.BusScheduleUpdateRequest;
import com.fpt.bbusbe.model.dto.request.busSchedule.CompleteBusScheduleRequest;
import com.fpt.bbusbe.model.dto.response.busSchedule.BusScheduleDatePageResponse;
import com.fpt.bbusbe.model.dto.response.busSchedule.BusSchedulePageResponse;
import com.fpt.bbusbe.model.dto.response.busSchedule.BusScheduleResponse;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface BusScheduleService {
    BusSchedulePageResponse findAll(int page, int size);

    int assignSchedulesForAllBusesOnDates(List<LocalDate> dates);

    BusSchedulePageResponse findByMonth(String month, String sort, int page, int size);

    BusScheduleDatePageResponse findDatesByMonth(String month, String sort, int page, int size);

    void deleteAllSchedulesByDate(LocalDate date);

    BusScheduleResponse findById(UUID id);

    BusScheduleResponse save(BusScheduleCreationRequest req);

    BusScheduleResponse update(BusScheduleUpdateRequest req);

    void changeStatus(@Valid BusScheduleStatusChangeRequest req);

    void delete(UUID id);

    BusScheduleResponse findByEspIdForMqtt(String number);

    void findByDate(Date date);

    void completeBusSchedule(CompleteBusScheduleRequest req);

}
