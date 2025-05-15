package com.fpt.bbusbe.model.dto.response.bus;

import com.fpt.bbusbe.model.enums.BusStatus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@Data

public class BusResponse {
    private UUID id;
    private String licensePlate;
    private String name;
    private UUID driverId;
    private String driverName;
    private String driverPhone;
    private UUID assistantId;
    private String assistantName;
    private String assistantPhone;
    private int amountOfStudents;
    private UUID routeId;
    private String routeCode;
    private String espId;
    private String cameraFacesluice;
    private BusStatus busStatus;
}
