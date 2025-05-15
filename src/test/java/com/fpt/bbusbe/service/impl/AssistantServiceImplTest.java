package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.model.dto.response.assistant.AssistantPageResponse;
import com.fpt.bbusbe.model.entity.Assistant;
import com.fpt.bbusbe.repository.AssistantRepository;
import com.fpt.bbusbe.repository.BusScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AssistantServiceImplTest {

    @Mock
    private BusScheduleRepository busScheduleRepository;

    @Mock
    private AssistantRepository assistantRepository;

    @InjectMocks
    private AssistantServiceImpl assistantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        Page<Assistant> mockPage = new PageImpl<>(Collections.emptyList());
        when(assistantRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        AssistantPageResponse response = assistantService.findAll("", "", 1, 10);

        assertNotNull(response);
        verify(assistantRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testFindAvailableAssistants() {
        Page<Assistant> mockPage = new PageImpl<>(Collections.emptyList());
        when(assistantRepository.findAvailableAssistants(anyString(), any(Pageable.class))).thenReturn(mockPage);

        AssistantPageResponse response = assistantService.findAvailableAssistants("", "", 1, 10);

        assertNotNull(response);
        verify(assistantRepository, times(1)).findAvailableAssistants(anyString(), any(Pageable.class));
    }
}
