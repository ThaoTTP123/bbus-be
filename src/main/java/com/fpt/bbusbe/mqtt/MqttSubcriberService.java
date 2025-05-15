package com.fpt.bbusbe.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.bbusbe.config.WebSocketHandler;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.firebase.FirebaseMessagingService;
import com.fpt.bbusbe.model.dto.response.bus.BusResponse;
import com.fpt.bbusbe.model.entity.Bus;
import com.fpt.bbusbe.model.entity.Camera;
import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.model.mqtt.*;
import com.fpt.bbusbe.repository.UserRepository;
import com.fpt.bbusbe.service.AttendanceService;
import com.fpt.bbusbe.service.BusService;
import com.fpt.bbusbe.service.CameraService;
import com.fpt.bbusbe.service.MqttService;
import com.fpt.bbusbe.utils.DateTimeUtils;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MqttSubcriberService {

    private final IMqttClient mqttClient;
    private final BusService busService;
    private final MqttService mqttService;

    @Value("${mqtt.topic}")
    private String topic;

    private final ObjectMapper objectMapper;

    private final CameraService cameraService;

    @PostConstruct
    public void subscribe() throws MqttException {
        if (!mqttClient.isConnected()) {
            throw new IllegalStateException("MQTT client is not connected!");
        }

        mqttClient.subscribe(topic, (topicReceive, message) -> {
            String payload = new String(message.getPayload());
            if (topicReceive.startsWith("mqtt/face/basic")) {
                // Process basic message
                BasicMessage basicMessage = objectMapper.readValue(payload, BasicMessage.class);
                cameraService.handleBasicMessage(basicMessage);
            } else if (topicReceive.startsWith("mqtt/face/heartbeat")) {
                // Process heartbeat message
                HeartbeatMessage heartbeatMessage = objectMapper.readValue(payload, HeartbeatMessage.class);
                cameraService.handleHeartbeatMessage(heartbeatMessage);
            } else if (topicReceive.matches("mqtt/face/\\d+/Rec")) {
                AttendanceMessage attendanceMessage = objectMapper.readValue(payload, AttendanceMessage.class);
                mqttService.handleAttendanceMessage(attendanceMessage, topicReceive);

            } else if (topicReceive.matches("mqtt/face/\\d+/Ack")) {
                ResultMessage resultMessage = objectMapper.readValue(payload, ResultMessage.class);
                mqttService.handleResultMessage(resultMessage, topicReceive);
            } else if (topicReceive.matches("mqtt/gps/\\d+/location")) {
                // Process GPS message
                BusLocationMessage busLocationMessage = objectMapper.readValue(payload, BusLocationMessage.class);
                busService.handleBusLocationMessage(busLocationMessage, topicReceive);
            } else {
                log.info("Unknown topic: " + topicReceive);
                log.info("Message: " + payload);
            }
        });
        log.info("Subscribed to topic: " + topic);
    }

}
