package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.model.dto.response.busSchedule.BusScheduleResponse;
import com.fpt.bbusbe.model.dto.response.driver.DriverPageResponse;
import com.fpt.bbusbe.model.entity.Driver;
import com.fpt.bbusbe.repository.BusScheduleRepository;
import com.fpt.bbusbe.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class DriverServiceImplTest {

    @Mock
    private BusScheduleRepository busScheduleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverServiceImpl driverService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        Page<Driver> mockPage = new PageImpl<>(Collections.emptyList());
        when(driverRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        DriverPageResponse response = driverService.findAll("", "", 1, 10);

        assertNotNull(response);
        verify(driverRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testFindAvailableDrivers() {
        Page<Driver> mockPage = new PageImpl<>(Collections.emptyList());
        when(driverRepository.findAvailableDrivers(anyString(), any(Pageable.class))).thenReturn(mockPage);

        DriverPageResponse response = driverService.findAvailableDrivers("", "", 1, 10);

        assertNotNull(response);
        verify(driverRepository, times(1)).findAvailableDrivers(anyString(), any(Pageable.class));
    }

    @Test
    void testFindScheduleByDate() {
        when(busScheduleRepository.findByDateAndDriver_User_Id(any(LocalDate.class), any(UUID.class)))
                .thenReturn(Collections.emptyList());

        List<BusScheduleResponse> response = driverService.findScheduleByDate(LocalDate.now());

        assertNotNull(response);
        verify(busScheduleRepository, times(1)).findByDateAndDriver_User_Id(any(LocalDate.class), any(UUID.class));
    }
}
