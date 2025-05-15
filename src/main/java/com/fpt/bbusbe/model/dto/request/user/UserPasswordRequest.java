package com.fpt.bbusbe.model.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
public class UserPasswordRequest {
    @NotNull(message = "id must not be null")
    private UUID id;

    @NotBlank(message = "id must not be blank")
    private String currentPassword;

    @NotBlank(message = "password must not be blank")
    private String password;

    @NotBlank(message = "confirmPassword must not be blank")
    private String confirmPassword;
}
