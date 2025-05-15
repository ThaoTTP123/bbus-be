package com.fpt.bbusbe.model.dto.request.bus;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Setter
@Getter
@ToString
public class BusUpdateRequest {

    private UUID id;

    @NotBlank(message = "License plate must not be blank")
    private String licensePlate;

    // @NotBlank(message = "Bus's name must not be blank")
    // private String name;

    private String assistantPhone;

    private String driverPhone;
}
