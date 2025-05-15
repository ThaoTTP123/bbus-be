package com.fpt.bbusbe.model.dto.response.route;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private UUID id;
    private String code;
    private String description;
    private String path;
    private String checkpointTime;
    private Date periodStart;
    private Date periodEnd;
    private Date createdAt;
    private Date updatedAt;
}
