package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.dto.response.assistant.AssistantPageResponse;
import com.fpt.bbusbe.model.dto.response.busSchedule.BusScheduleResponse;
import com.fpt.bbusbe.model.entity.BusSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AssistantService {

    AssistantPageResponse findAll(String keyword, String sort, int page, int size);

    List<BusScheduleResponse> findScheduleByDate(LocalDate date);


    AssistantPageResponse findAvailableAssistants(String keyword, String sort, int page, int size);

    List<BusScheduleResponse> findScheduleByMonth(int year, int month);
}
