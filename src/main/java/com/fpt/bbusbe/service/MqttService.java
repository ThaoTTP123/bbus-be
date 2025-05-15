package com.fpt.bbusbe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.bbusbe.model.entity.Student;
import com.fpt.bbusbe.model.enums.Role;
import com.fpt.bbusbe.model.mqtt.AttendanceMessage;
import com.fpt.bbusbe.model.mqtt.OperationType;
import com.fpt.bbusbe.model.mqtt.ResultMessage;

import java.util.List;
import java.util.UUID;

public interface MqttService {
    void publishStudentsList(List<Student> students, OperationType type, String facesluice) throws JsonProcessingException;

    void handleAttendanceMessage(AttendanceMessage message, String topic) throws JsonProcessingException;

    void handleResultMessage(ResultMessage resultMessage, String topicReceive);
}
