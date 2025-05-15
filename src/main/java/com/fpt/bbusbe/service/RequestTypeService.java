package com.fpt.bbusbe.service;


import com.fpt.bbusbe.model.dto.response.requestType.RequestTypePageResponse;
import com.fpt.bbusbe.model.dto.response.requestType.RequestTypeResponse;

import java.util.UUID;

public interface RequestTypeService {

    RequestTypePageResponse findAll(String keyword, String sort, int page, int size);

    RequestTypeResponse findById(UUID id);

//    Request save(RequestCreationRequest req);

//    void delete(UUID id);
}
