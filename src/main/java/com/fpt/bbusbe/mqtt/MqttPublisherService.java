package com.fpt.bbusbe.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MqttPublisherService {

    private final IMqttClient mqttClient;
    private final ObjectMapper objectMapper;

    public void publishMessage(String topic, Object payload) throws MqttException, JsonProcessingException {
        String frontTopic = "mqtt/face/";
        String jsonMessage = objectMapper.writeValueAsString(payload);
        MqttMessage message = new MqttMessage(jsonMessage.getBytes());
        message.setQos(1); // QoS 1: At least once
        message.setRetained(false);

        mqttClient.publish(frontTopic + topic, message);
    }

    public void publishMessageCustomTopic(String topic, Object payload) throws MqttException, JsonProcessingException {
        String jsonMessage = objectMapper.writeValueAsString(payload);
        MqttMessage message = new MqttMessage(jsonMessage.getBytes());
        message.setQos(1); // QoS 1: At least once
        message.setRetained(false);

        mqttClient.publish(topic, message);
    }
}
