package com.fpt.bbusbe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.bbusbe.model.dto.response.cameraRequest.CameraRequestPageResponse;
import com.fpt.bbusbe.model.dto.response.cameraRequest.CameraRequestResponse;

import java.util.UUID;

public interface CameraRequestService {
    CameraRequestPageResponse findAll(String keyword, String sort, int page, int size);

    CameraRequestResponse findById(UUID id);

    void save(UUID cameraRequestId);

    void uploadAllUnsuccessfulCameraRequestDetails() throws JsonProcessingException;
}
