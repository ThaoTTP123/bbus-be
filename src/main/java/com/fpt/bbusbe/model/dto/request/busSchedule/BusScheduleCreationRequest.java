package com.fpt.bbusbe.model.dto.request.busSchedule;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BusScheduleCreationRequest implements Serializable {

    @NotBlank(message = "License plate must not be blank")
    private String licensePlate;

    @NotBlank(message = "Bus's name must not be blank")
    private String name;

    @NotBlank(message = "Assistant's phone number must not be blank")
    private String assistantPhone;

    @NotBlank(message = "Driver's phone number must not be blank")
    private String driverPhone;

}
