package com.fpt.bbusbe.model.dto.response.requestType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@Builder
public class RequestTypeResponse {
    private UUID requestTypeId;
    private String requestTypeName;
}
