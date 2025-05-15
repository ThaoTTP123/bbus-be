package com.fpt.bbusbe.model.dto.response.bus;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusReport {
    private String licensePlate;
    private String driverName;
    private String assistantName;
    private long amountOfRide;
    private int maxCapacity;
}
