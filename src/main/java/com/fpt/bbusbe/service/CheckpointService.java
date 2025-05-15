package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointCreationRequest;
import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointStatusChangeRequest;
import com.fpt.bbusbe.model.dto.request.checkpoint.CheckpointUpdateRequest;
import com.fpt.bbusbe.model.dto.response.checkpoint.*;
import com.fpt.bbusbe.model.entity.Checkpoint;

import java.util.List;
import java.util.UUID;

public interface CheckpointService {

    CheckpointPageResponse findAll(String keyword, String sort, int page, int size);

    CheckpointResponse findById(UUID id);

    CheckpointResponse save(CheckpointCreationRequest req);

    CheckpointResponse update(CheckpointUpdateRequest req);

    void changeStatus(CheckpointStatusChangeRequest req);

    void delete(UUID id);

    Checkpoint findByName(String name);

    List<CheckpointWithRegisteredResponse> findAllWithAmountOfStudentRegister(String keyword);

    CheckpointWithStudentAndBus getDetailedWithStudentAndBus(UUID checkpointId);

    List<CheckpointWithStudentResponse> getStudentsByCheckpoint(UUID checkpointId);

    int countStudentsInCheckpoint(UUID checkpointId);

    List<CheckpointWithTimeResponse> getCheckpointsByRoute(UUID routeId);

    List<CheckpointResponse> getCheckpointsWithoutRoute();

    List<CheckpointResponse> getCheckpointsWithRoute();

    void toggleCheckpointStatus(UUID checkpointId);

    CheckpointWithAmountStudentPageResponse getAnCheckpointWithStudentCount(String keyword, String sort, int page, int size);
}
