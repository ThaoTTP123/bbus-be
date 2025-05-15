package com.fpt.bbusbe.model.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String sessionId;
    private String password;
    private String confirmPassword;
}


