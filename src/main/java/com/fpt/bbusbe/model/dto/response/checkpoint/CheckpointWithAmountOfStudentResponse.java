package com.fpt.bbusbe.model.dto.response.checkpoint;

import com.fpt.bbusbe.model.enums.CheckpointStatus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@Data
public class CheckpointWithAmountOfStudentResponse {
    private UUID id;
    private String name;
    private String description;
    private String latitude;
    private String longitude;
    private CheckpointStatus status;
    private long amountOfStudent;
}
