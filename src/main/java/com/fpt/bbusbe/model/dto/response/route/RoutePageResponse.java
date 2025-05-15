package com.fpt.bbusbe.model.dto.response.route;

import com.fpt.bbusbe.model.dto.response.PageResponseAbstract;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class RoutePageResponse extends PageResponseAbstract implements Serializable {
    private List<RouteResponse> routes;
}
