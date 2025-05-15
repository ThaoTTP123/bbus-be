package com.fpt.bbusbe.model.dto.request.route;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RouteCreationRequest {
    private String code;
    private String path;
    private String checkpointTime;
    private String description;
    private Date periodStart;
    private Date periodEnd;
}
