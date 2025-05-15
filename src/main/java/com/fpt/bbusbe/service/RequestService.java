package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.dto.request.request.ReplyRequestRequest;
import com.fpt.bbusbe.model.dto.request.request.RequestCreationRequest;
import com.fpt.bbusbe.model.dto.response.bus.BusWithCheckpointResponse;
import com.fpt.bbusbe.model.dto.response.request.RequestPageResponse;
import com.fpt.bbusbe.model.dto.response.request.RequestResponse;

import java.util.List;
import java.util.UUID;

public interface RequestService {

    RequestPageResponse findAll(String keyword, String sort, int page, int size);

    RequestResponse findById(UUID id);

    RequestResponse save(RequestCreationRequest req);

    RequestResponse replyRequest(ReplyRequestRequest req);

    void delete(UUID id);

    BusWithCheckpointResponse processChangeCheckpointRequest(UUID requestId);

    List<RequestResponse> getMyRequests(UUID requestTypeId);

    long countPendingRequests();
    long countTotalRequests();
}
