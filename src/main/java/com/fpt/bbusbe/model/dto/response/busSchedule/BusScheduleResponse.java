package com.fpt.bbusbe.model.dto.response.busSchedule;

import com.fpt.bbusbe.model.entity.BusSchedule;
import com.fpt.bbusbe.model.enums.BusDirection;
import com.fpt.bbusbe.model.enums.BusScheduleStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusScheduleResponse {
    private UUID id;
    private UUID busId;
    private String name;
    private String licensePlate;
    private LocalDate date;
    private UUID driverId;
    private String driverName;
    private UUID assistantId;
    private String assistantName;
    private String route;
    private UUID routeId;
    private BusDirection direction;
    private Date createdAt;
    private Date updatedAt;
    private BusScheduleStatus busScheduleStatus;

    public BusScheduleResponse(BusSchedule busSchedule) {
        this.id = busSchedule.getId();
        this.busId = busSchedule.getBus().getId();
        this.name = busSchedule.getBus().getName();
        this.date = busSchedule.getDate();
        this.licensePlate = busSchedule.getBus().getLicensePlate() != null ? busSchedule.getBus().getLicensePlate() : null;
        this.driverId = busSchedule.getDriver() != null ? busSchedule.getDriver().getUser().getId() : null;
        this.driverName = busSchedule.getDriver() != null ? busSchedule.getDriver().getUser().getName() : null;
        this.assistantId = busSchedule.getAssistant() != null ? busSchedule.getAssistant().getUser().getId() : null;
        this.assistantName = busSchedule.getAssistant() != null ? busSchedule.getAssistant().getUser().getName() : null;
        this.route = busSchedule.getRoute() != null ? busSchedule.getRoute().getCode() : null;
        this.routeId = busSchedule.getRoute() != null ? busSchedule.getRoute().getId() : null;
        this.direction = busSchedule.getDirection() != null ? busSchedule.getDirection() : null;
        this.createdAt = busSchedule.getCreatedAt();
        this.updatedAt = busSchedule.getUpdatedAt();
        this.busScheduleStatus = busSchedule.getBusScheduleStatus();
    }

}
