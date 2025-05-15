package com.fpt.bbusbe.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.db.AttendanceDTO;
import com.fpt.bbusbe.model.dto.response.attendance.AttendanceResponse;
import com.fpt.bbusbe.model.entity.Attendance;
import com.fpt.bbusbe.model.enums.BusDirection;
import com.fpt.bbusbe.model.mqtt.ActiveGpsModuleMessage;
import com.fpt.bbusbe.mqtt.MqttPublisherService;
import com.fpt.bbusbe.repository.AttendanceRepository;
import com.fpt.bbusbe.service.AttendanceService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j(topic = "WEBSOCKET-HANDLER")
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private static final String TOPIC_MQTT_ACTIVE = "mqtt/gps/{espId}/active";
    private static final String TOPIC_SOCKET_STUDENT_STATUS = "/student-status";

    // topic -> set of sessions
    private final Map<String, Set<WebSocketSession>> topicSessions = new ConcurrentHashMap<>();
    private final MqttPublisherService mqttPublisherService;
    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;
    private final ObjectMapper objectMapper;

//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String topic = extractTopic(session);
//        Set<WebSocketSession> sessions = topicSessions.get(topic);
//        if (topic.endsWith(TOPIC_SOCKET_STUDENT_STATUS)) {
//            log.info("received in {} from topic: {}", topic, message.getPayload());
//        }
//    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String topic = extractTopic(session);
        topicSessions.putIfAbsent(topic, Collections.synchronizedSet(new HashSet<>()));

        Set<WebSocketSession> sessions = topicSessions.get(topic);
        if (topic.length() == 6) {
            synchronized (sessions) {
                int previousSize = sessions.size();
                sessions.add(session);
                int currentSize = sessions.size();

                if (previousSize == 0 && currentSize == 1) {
                    activeGpsModule(topic); // 0 → 1
                }
            }
        }

        if (topic.endsWith(TOPIC_SOCKET_STUDENT_STATUS)) {
            synchronized (sessions) {
                //add session to topic
                sessions.add(session);

                publishCurrentStudentStatus(topic);
            }
        }

        session.sendMessage(new TextMessage("Connected to topic: " + topic));
        log.info("Session {} connected to topic {}", session.getId(), topic);
    }

    private void publishCurrentStudentStatus(String topic) throws JsonProcessingException {
        String[] parts = topic.split("/");
        UUID busId = UUID.fromString(parts[0]);
        String directionString = parts[1];
        BusDirection direction = "0".equals(directionString) ? BusDirection.PICK_UP : BusDirection.DROP_OFF;

        if (!"0".equals(directionString) && !"1".equals(directionString)) {
            throw new ResourceNotFoundException("Direction is invalid");
        }
        LocalDate currentDate = LocalDate.now();
        List<Attendance> attendances = attendanceRepository.findAllByStudent_Checkpoint_Route_BusSchedules_Bus_IdAndDirectionAndDate(busId, direction, currentDate);
        if (attendances.isEmpty()) {
            log.info("No attendance data found for busId: {} and direction: {}", busId, direction);
            return;
        }
        List<AttendanceDTO> attendanceDTOs = attendances.stream()
                .map(attendanceService::convertToDTO)
                .collect(Collectors.toList());

        broadcast(topic, objectMapper.writeValueAsString(attendanceDTOs));
    }

    public void publishStudentStatus(String topic, AttendanceResponse attendance) throws JsonProcessingException {
        broadcast(topic, objectMapper.writeValueAsString(attendance));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String topic = extractTopic(session);
        Set<WebSocketSession> sessions = topicSessions.get(topic);

        if (topic.length() == 6) {
            if (sessions != null) {
                synchronized (sessions) {
                    sessions.remove(session);
                    int currentSize = sessions.size();

                    if (currentSize == 0) {
                        inActiveGpsModule(topic); // 1 → 0
                        topicSessions.remove(topic);
                    }
                }
            }
        }

        log.info("Session {} disconnected from topic {}", session.getId(), topic);
    }


    public void broadcast(String topic, String message) {
        Set<WebSocketSession> sessions = topicSessions.get(topic);
        if (sessions != null) {
            synchronized (sessions) {
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(message));
                        } catch (Exception e) {
                            log.error("Error sending message to topic {}: {}", topic, e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private String extractTopic(WebSocketSession session) {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String prefix = "/ws/";
        int index = path.indexOf(prefix);

        if (index == -1) {
            throw new IllegalArgumentException("Invalid URL: missing /ws/ prefix");
        }
        return path.substring(index + prefix.length());
    }

    private void activeGpsModule(String topic) {
        log.info("🟩 Topic [{}] has its first client. Triggering startup logic.", topic);
        // Your custom logic here
        try {
            topic = TOPIC_MQTT_ACTIVE.replace("{espId}", topic);
            ActiveGpsModuleMessage payload = new ActiveGpsModuleMessage(true);
            mqttPublisherService.publishMessageCustomTopic(topic, payload);
        } catch (MqttException | JsonProcessingException e) {
            log.error("Error occurred while publishing MQTT message for topic {}: {}", topic, e.getMessage(), e);
        }
    }

    private void inActiveGpsModule(String topic) {
        log.info("🟥 Topic [{}] is now empty. Triggering shutdown logic.", topic);
        // Your custom logic here
        try {
            topic = TOPIC_MQTT_ACTIVE.replace("{espId}", topic);
            ActiveGpsModuleMessage payload = new ActiveGpsModuleMessage(false);
            mqttPublisherService.publishMessageCustomTopic(topic, payload);
        } catch (MqttException | JsonProcessingException e) {
            log.error("Error occurred while publishing MQTT message for topic {}: {}", topic, e.getMessage(), e);
        }
    }


}
