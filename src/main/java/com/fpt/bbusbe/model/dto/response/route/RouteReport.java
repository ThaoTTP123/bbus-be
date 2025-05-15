package com.fpt.bbusbe.model.dto.response.route;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteReport {
    private String routeName;
    private String path;
    private long amountOfStudent;
    private long amountOfTrip;
}
