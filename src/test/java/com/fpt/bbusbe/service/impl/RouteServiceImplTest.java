package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.response.route.RouteResponse;
import com.fpt.bbusbe.model.entity.Bus;
import com.fpt.bbusbe.model.entity.Route;
import com.fpt.bbusbe.repository.BusRepository;
import com.fpt.bbusbe.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteServiceImplTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private BusRepository busRepository;

    @InjectMocks
    private RouteServiceImpl routeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Success() {
        UUID routeId = UUID.randomUUID();
        Route mockRoute = new Route();
        mockRoute.setId(routeId);

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(mockRoute));

        RouteResponse response = routeService.findById(routeId);

        assertNotNull(response);
        assertEquals(routeId, response.getId());
        verify(routeRepository, times(1)).findById(routeId);
    }

    @Test
    void testFindById_NotFound() {
        UUID routeId = UUID.randomUUID();

        when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> routeService.findById(routeId));
    }

    @Test
    void testGetRoutePathByBusId_Success() {
        UUID busId = UUID.randomUUID();
        Bus mockBus = new Bus();
        Route mockRoute = new Route();
        mockRoute.setPath("Checkpoint1 Checkpoint2");
        mockBus.setRoute(mockRoute);

        when(busRepository.findById(busId)).thenReturn(Optional.of(mockBus));

        String path = routeService.getRoutePathByBusId(busId);

        assertNotNull(path);
        assertEquals("Checkpoint1 Checkpoint2", path);
        verify(busRepository, times(1)).findById(busId);
    }

    @Test
    void testGetRoutePathByBusId_BusNotFound() {
        UUID busId = UUID.randomUUID();

        when(busRepository.findById(busId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> routeService.getRoutePathByBusId(busId));
    }

    @Test
    void testGetRoutePathByBusId_NoRouteAssigned() {
        UUID busId = UUID.randomUUID();
        Bus mockBus = new Bus();

        when(busRepository.findById(busId)).thenReturn(Optional.of(mockBus));

        assertThrows(ResourceNotFoundException.class, () -> routeService.getRoutePathByBusId(busId));
    }
}
