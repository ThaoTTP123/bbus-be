package com.fpt.bbusbe.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.bbusbe.model.entity.Student;
import com.fpt.bbusbe.model.mqtt.OperationType;
import com.fpt.bbusbe.mqtt.MqttPublisherService;
import com.fpt.bbusbe.repository.CameraRepository;
import com.fpt.bbusbe.repository.CameraRequestRepository;
import com.fpt.bbusbe.repository.CameraRequestDetailRepository;
import com.fpt.bbusbe.repository.StudentRepository;
import com.fpt.bbusbe.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MqttServiceImplTest {

    @Mock
    private CameraRepository cameraRepository;

    @Mock
    private MqttPublisherService mqttPublisherService;

    @Mock
    private S3Service s3Service;

    @Mock
    private CameraRequestRepository cameraRequestRepository;

    @Mock
    private CameraRequestDetailRepository cameraRequestDetailRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private MqttServiceImpl mqttService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPublishStudentsList_Success() throws Exception {
        List<Student> students = Collections.emptyList();
        mqttService.publishStudentsList(students, OperationType.ADD, "testFacesluice");
        verify(mqttPublisherService, times(1)).publishMessage(anyString(), any());
    }

    @Test
    void testPublishStudentsList_NoStudents() throws Exception {
        mqttService.publishStudentsList(Collections.emptyList(), OperationType.ADD, "testFacesluice");
        verify(mqttPublisherService, never()).publishMessage(anyString(), any());
    }

    @Test
    void testHandleAttendanceMessage_InvalidStudent() throws JsonProcessingException {
        assertThrows(RuntimeException.class, () -> mqttService.handleAttendanceMessage(null, "testTopic"));
    }
}
