package com.fpt.bbusbe.model.dto.request.request;

import com.fpt.bbusbe.model.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ReplyRequestRequest {
    private UUID requestId;
    private UUID approvedByUserId; //optional
//    private UUID checkpointId;
    private String reply;
    private RequestStatus status;
}
