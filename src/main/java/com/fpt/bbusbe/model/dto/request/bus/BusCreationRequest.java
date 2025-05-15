package com.fpt.bbusbe.model.dto.request.bus;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BusCreationRequest implements Serializable {

    @NotBlank(message = "License plate must not be blank")
    private String licensePlate;

    @NotBlank(message = "Bus's name must not be blank")
    private String name;

    @NotBlank(message = "Assistant's phone number must not be blank")
    private String assistantPhone;

    @NotBlank(message = "Driver's phone number must not be blank")
    private String driverPhone;

    private UUID route;

}
