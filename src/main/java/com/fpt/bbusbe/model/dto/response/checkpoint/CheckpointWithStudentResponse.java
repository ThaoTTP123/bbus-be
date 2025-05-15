package com.fpt.bbusbe.model.dto.response.checkpoint;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CheckpointWithStudentResponse {
    private UUID studentId;
    private String studentName;
    private String rollNumber;
    private boolean registered;
    private UUID busId;
    private String busName;
}
