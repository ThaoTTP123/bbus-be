package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.dto.request.route.RouteCreationRequest;
import com.fpt.bbusbe.model.dto.request.route.RouteUpdateRequest;
import com.fpt.bbusbe.model.dto.response.route.RoutePageResponse;
import com.fpt.bbusbe.model.dto.response.route.RouteResponse;
import com.fpt.bbusbe.model.dto.response.route.RouteResponseUpdate;

import java.util.UUID;

public interface RouteService {
    RoutePageResponse findAll(String keyword, String sort, int page, int size);

    RouteResponse findById(UUID id);

    RouteResponse save(RouteCreationRequest req);

    RouteResponseUpdate updateInfoAndCheckpoints(RouteUpdateRequest request);

    void deleteRoute(UUID routeId);

    String getRoutePathByBusId(UUID busId);

    long countTotalRoutes();

}