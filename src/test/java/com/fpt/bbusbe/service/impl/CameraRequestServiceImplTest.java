package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.repository.CameraRequestRepository;
import com.fpt.bbusbe.service.MqttService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class CameraRequestServiceImplTest {

    @Mock
    private CameraRequestRepository cameraRequestRepository;

    @Mock
    private MqttService mqttService;

    @InjectMocks
    private CameraRequestServiceImpl cameraRequestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadAllUnsuccessfulCameraRequestDetails() throws Exception {
        cameraRequestService.uploadAllUnsuccessfulCameraRequestDetails();
        verify(mqttService, atLeastOnce()).publishStudentsList(any(), any(), any());
    }
}
