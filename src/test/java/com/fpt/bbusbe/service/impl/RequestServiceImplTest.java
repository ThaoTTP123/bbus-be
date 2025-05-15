package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.request.RequestCreationRequest;
import com.fpt.bbusbe.model.dto.response.request.RequestResponse;
import com.fpt.bbusbe.model.entity.Request;
import com.fpt.bbusbe.model.entity.RequestType;
import com.fpt.bbusbe.model.enums.RequestStatus;
import com.fpt.bbusbe.repository.RequestRepository;
import com.fpt.bbusbe.repository.RequestTypeRepository;
import com.fpt.bbusbe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestTypeRepository requestTypeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestServiceImpl requestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Success() {
        UUID requestId = UUID.randomUUID();
        Request mockRequest = new Request();
        mockRequest.setId(requestId);
        mockRequest.setStatus(RequestStatus.PENDING);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(mockRequest));

        RequestResponse response = requestService.findById(requestId);

        assertNotNull(response);
        assertEquals(requestId, response.getRequestId());
        verify(requestRepository, times(1)).findById(requestId);
    }

    @Test
    void testFindById_NotFound() {
        UUID requestId = UUID.randomUUID();

        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> requestService.findById(requestId));
    }

    @Test
    void testSave_Success() {
        RequestCreationRequest request = new RequestCreationRequest();
        request.setRequestTypeId(UUID.randomUUID());

        RequestType mockRequestType = new RequestType();
        mockRequestType.setRequestTypeName("Test Request");

        when(requestTypeRepository.getOne(request.getRequestTypeId())).thenReturn(mockRequestType);

        RequestResponse response = requestService.save(request);

        assertNotNull(response);
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    @Test
    void testSave_InvalidRequestType() {
        RequestCreationRequest request = new RequestCreationRequest();

        assertThrows(ResourceNotFoundException.class, () -> requestService.save(request));
    }
}
