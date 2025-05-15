package com.fpt.bbusbe.model.dto.response.request;

import com.fpt.bbusbe.model.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RequestResponse {
    private UUID requestId;
    private UUID requestTypeId;
    private String requestTypeName;
    private UUID studentId;
    private String studentName;
    private UUID sendByUserId;
    private String sendByName;
    private UUID checkpointId;
    private String checkpointName;
    private UUID approvedByUserId;
    private String approvedByName;
    private Date fromDate;
    private Date toDate;
    private String reason;
    private String reply;
    private RequestStatus status;
    private Date createdAt;
    private Date updatedAt;
}
