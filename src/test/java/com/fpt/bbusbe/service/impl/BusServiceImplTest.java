package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.bus.BusCreationRequest;
import com.fpt.bbusbe.model.entity.Bus;
import com.fpt.bbusbe.repository.BusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BusServiceImplTest {

    @Mock
    private BusRepository busRepository;

    @InjectMocks
    private BusServiceImpl busService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Success() {
        UUID busId = UUID.randomUUID();
        Bus bus = new Bus();
        bus.setId(busId);

        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));

        assertNotNull(busService.findById(busId));
        verify(busRepository, times(1)).findById(busId);
    }

    @Test
    void testFindById_NotFound() {
        UUID busId = UUID.randomUUID();

        when(busRepository.findById(busId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> busService.findById(busId));
    }

    @Test
    void testSave_Success() {
        BusCreationRequest request = new BusCreationRequest();
        request.setName("Bus 1");

        Bus bus = new Bus();
        bus.setName("Bus 1");

        when(busRepository.save(any(Bus.class))).thenReturn(bus);

        assertNotNull(busService.save(request));
        verify(busRepository, times(1)).save(any(Bus.class));
    }
}
