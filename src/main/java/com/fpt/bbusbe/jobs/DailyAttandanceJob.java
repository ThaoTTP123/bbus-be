package com.fpt.bbusbe.jobs;

import com.fpt.bbusbe.repository.BusScheduleRepository;
import com.fpt.bbusbe.service.BusScheduleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DailyAttandanceJob {

    private final BusScheduleService busScheduleService;

    public DailyAttandanceJob(BusScheduleService busScheduleService) {
        this.busScheduleService = busScheduleService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void initDailyBusScheduleForAssistantAndDriver() {
        Date date = new Date();
        busScheduleService.findByDate(date);
    }
}
