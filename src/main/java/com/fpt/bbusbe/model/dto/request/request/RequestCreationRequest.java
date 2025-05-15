package com.fpt.bbusbe.model.dto.request.request;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreationRequest {
    private UUID studentId;
    private UUID sendByUserId;
    private UUID requestTypeId;
    private UUID checkpointId;
    private String reason;
    private Date fromDate;
    private Date toDate;
}
