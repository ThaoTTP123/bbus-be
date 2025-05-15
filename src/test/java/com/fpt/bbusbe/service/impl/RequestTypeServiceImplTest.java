package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.response.requestType.RequestTypeResponse;
import com.fpt.bbusbe.model.entity.RequestType;
import com.fpt.bbusbe.repository.RequestTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestTypeServiceImplTest {

    @Mock
    private RequestTypeRepository requestTypeRepository;

    @InjectMocks
    private RequestTypeServiceImpl requestTypeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Success() {
        UUID requestTypeId = UUID.randomUUID();
        RequestType mockRequestType = new RequestType();
        mockRequestType.setId(requestTypeId);

        when(requestTypeRepository.findById(requestTypeId)).thenReturn(Optional.of(mockRequestType));

        RequestTypeResponse response = requestTypeService.findById(requestTypeId);

        assertNotNull(response);
        assertEquals(requestTypeId, response.getRequestTypeId());
        verify(requestTypeRepository, times(1)).findById(requestTypeId);
    }

    @Test
    void testFindById_NotFound() {
        UUID requestTypeId = UUID.randomUUID();

        when(requestTypeRepository.findById(requestTypeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> requestTypeService.findById(requestTypeId));
    }
}
