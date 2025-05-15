package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.dto.request.student.StudentUpdateByParentRequest;
import com.fpt.bbusbe.model.dto.response.bus.BusResponse;
import com.fpt.bbusbe.model.dto.response.parent.ParentPageResponse;
import com.fpt.bbusbe.model.dto.response.student.StudentResponse;

import java.util.List;
import java.util.UUID;

public interface ParentService {

    List<StudentResponse> findStudentsOfAParent();

    ParentPageResponse findAll(String keyword, String sort, int page, int size);

    BusResponse registerCheckpoint(UUID studentId, UUID checkpointId);

    List<BusResponse> registerCheckpointForAllChildren(UUID checkpointId);

    void updateStudentInfo(StudentUpdateByParentRequest request);

    BusResponse upsertCheckpoint(UUID studentId, UUID newCheckpointId);

    List<BusResponse> upsertCheckpointForAll(UUID newCheckpointId);

}
