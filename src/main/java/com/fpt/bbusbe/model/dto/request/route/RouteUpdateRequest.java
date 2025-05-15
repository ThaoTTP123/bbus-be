package com.fpt.bbusbe.model.dto.request.route;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Data
public class RouteUpdateRequest {
    private UUID routeId;
    private String description;
    private List<UUID> orderedCheckpointIds;
    private List<String> orderedCheckpointTimes;
}
