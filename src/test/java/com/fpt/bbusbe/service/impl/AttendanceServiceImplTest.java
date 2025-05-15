package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.response.attendance.AttendanceResponse;
import com.fpt.bbusbe.model.entity.Attendance;
import com.fpt.bbusbe.repository.AttendanceRepository;
import com.fpt.bbusbe.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAllByBusIdAndDateAndDirection() {
        when(attendanceRepository.findAllByBusIdAndDateAndDirectionOrderByStudent_Checkpoint_Name(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<AttendanceResponse> responses = attendanceService.findAllByBusIdAndDateAndDirection(
                UUID.randomUUID(), LocalDate.now(), null);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(attendanceRepository, times(1))
                .findAllByBusIdAndDateAndDirectionOrderByStudent_Checkpoint_Name(any(), any(), any());
    }

    @Test
    void testGetAttendanceHistoryOfAStudentForParent_NotFound() {
        when(attendanceRepository.findAllByStudent_IdAndDate(any(), any())).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () ->
                attendanceService.getAttendanceHistoryOfAStudentForParent(UUID.randomUUID(), LocalDate.now()));
    }
}
