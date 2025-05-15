package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.busSchedule.CompleteBusScheduleRequest;
import com.fpt.bbusbe.model.entity.Attendance;
import com.fpt.bbusbe.model.entity.BusSchedule;
import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusScheduleStatus;
import com.fpt.bbusbe.repository.AttendanceRepository;
import com.fpt.bbusbe.repository.BusRepository;
import com.fpt.bbusbe.repository.BusScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BusScheduleServiceImplTest {

    @Mock
    private BusScheduleRepository busScheduleRepository;

    @Mock
    private BusRepository busRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private BusScheduleServiceImpl busScheduleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCompleteBusSchedule_Success() {
        UUID scheduleId = UUID.randomUUID();
        CompleteBusScheduleRequest request = new CompleteBusScheduleRequest();
        request.setBusScheduleId(scheduleId);
        request.setNote("Completed successfully");

        BusSchedule busSchedule = new BusSchedule();
        busSchedule.setId(scheduleId);
        busSchedule.setBusScheduleStatus(BusScheduleStatus.PENDING);

        when(busScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(busSchedule));
        when(attendanceRepository.findAllByBusSchedule(any(), any(), any())).thenReturn(Collections.emptyList());

        busScheduleService.completeBusSchedule(request);

        assertEquals(BusScheduleStatus.COMPLETED, busSchedule.getBusScheduleStatus());
        verify(busScheduleRepository, times(1)).save(busSchedule);
    }

    @Test
    void testCompleteBusSchedule_StudentsInBus() {
        UUID scheduleId = UUID.randomUUID();
        CompleteBusScheduleRequest request = new CompleteBusScheduleRequest();
        request.setBusScheduleId(scheduleId);

        BusSchedule busSchedule = new BusSchedule();
        busSchedule.setId(scheduleId);

        when(busScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(busSchedule));
        when(attendanceRepository.findAllByBusSchedule(any(), any(), any()))
                .thenReturn(Collections.singletonList(new Attendance()));

        assertThrows(InvalidDataException.class, () -> busScheduleService.completeBusSchedule(request));
    }

    @Test
    void testCompleteBusSchedule_NotFound() {
        UUID scheduleId = UUID.randomUUID();
        CompleteBusScheduleRequest request = new CompleteBusScheduleRequest();
        request.setBusScheduleId(scheduleId);

        when(busScheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> busScheduleService.completeBusSchedule(request));
    }
}
