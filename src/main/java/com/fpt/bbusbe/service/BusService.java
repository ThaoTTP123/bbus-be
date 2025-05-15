package com.fpt.bbusbe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.bbusbe.model.dto.request.bus.BusCreationRequest;
import com.fpt.bbusbe.model.dto.request.bus.BusStatusChangeRequest;
import com.fpt.bbusbe.model.dto.request.bus.BusUpdateRequest;
import com.fpt.bbusbe.model.dto.response.bus.BusPageResponse;
import com.fpt.bbusbe.model.dto.response.bus.BusResponse;
import com.fpt.bbusbe.model.mqtt.AttendanceMessage;
import com.fpt.bbusbe.model.mqtt.BusLocationMessage;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface BusService {
    BusPageResponse findAll(String keyword, String sort, int page, int size);

    BusResponse findById(UUID id);

    BusResponse save(BusCreationRequest req);

    BusResponse update(BusUpdateRequest req);

    void changeStatus(@Valid BusStatusChangeRequest req);

    void delete(UUID id);

    void handleBusLocationMessage(BusLocationMessage busLocationMessage, String topic);

    void updateMaxCapacity(int maxCapacity);

    List<BusResponse> findBusesByCheckpointId(UUID checkpointId);

    List<BusResponse> findBusesByRouteId(UUID routeId);

}
