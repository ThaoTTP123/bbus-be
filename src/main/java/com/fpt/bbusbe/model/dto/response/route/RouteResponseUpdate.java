package com.fpt.bbusbe.model.dto.response.route;

import com.fpt.bbusbe.model.enums.CheckpointStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RouteResponseUpdate {
    private UUID id;
    private String code;
    private String description;
    private List<CheckpointResponse> checkpoints;
    private List<String> orderedCheckpointTimes;

    @Data
    @Builder
    public static class CheckpointResponse {
        private UUID id;
        private String name;
        private String description;
        private String latitude;
        private String longitude;
        private CheckpointStatus status;
    }
}

