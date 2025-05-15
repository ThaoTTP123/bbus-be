package com.fpt.bbusbe.config;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker-url}")  // Inject the broker URL from application.yml
    private String brokerUrl;

    @Value("${mqtt.client-id}")  // Inject the client ID from application.yml
    private String clientId;

    @Value("${mqtt.topic}")      // Inject the topic from application.yml
    private String topic;

    @Value("${mqtt.username:}")
    private String username;  // Optional: for authentication

    @Value("${mqtt.password:}")
    private String password;  // Optional: for authentication

    @Value("${mqtt.clean-session}")
    private boolean cleanSession;

    @Value("${mqtt.connection-timeout}")
    private int connectionTimeout;

    @Value("${mqtt.keep-alive}")
    private int keepAlive;

    @Bean
    public IMqttClient mqttClient() throws MqttException {
        // Create the MQTT client using the values from the application.yml file
        IMqttClient mqttClient = new MqttClient(brokerUrl, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(cleanSession);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAlive);

        // Optional: Set username and password if required by the broker
        if (!username.isEmpty() && !password.isEmpty()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        // Connect to the remote broker
        mqttClient.connect(options);

        return mqttClient;
    }
}
