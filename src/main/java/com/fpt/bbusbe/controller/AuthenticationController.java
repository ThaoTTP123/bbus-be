package com.fpt.bbusbe.controller;

import com.fpt.bbusbe.model.dto.request.auth.ResetPasswordRequest;
import com.fpt.bbusbe.model.dto.request.auth.SignInRequest;
import com.fpt.bbusbe.model.dto.response.auth.LoginResponse;
import com.fpt.bbusbe.model.dto.response.auth.TokenResponse;
import com.fpt.bbusbe.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
@Tag(name = "Authentication Controller")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

//    @Operation(summary = "Access Token", description = "Get access token and refresh token by username and password")
//    @PostMapping("access-token")
//    public TokenResponse getAccessToken(@RequestBody SignInRequest signInRequest) {
//        log.info("Access token request...");
//
//        return authenticationService.getAccessToken(signInRequest);
//    }

    @Operation(summary = "Login", description = "Get access token and refresh token by username and password")
    @PostMapping("login")
    public LoginResponse getAccessToken(@RequestBody @Valid SignInRequest signInRequest) {
        log.info("Login request...");

        return authenticationService.login(signInRequest);
    }

    @Operation(summary = "Refresh Token", description = "Get new access token by refresh token")
    @PostMapping("refresh-token")
    public TokenResponse getRefreshToken(
            @RequestBody
            @NotBlank(message = "Refresh token can't be null")
            String refresh_token
    ) {
        log.info("Refresh token request...");

        return authenticationService.getRefreshToken(refresh_token);
    }

    @PostMapping("/forgot-password/request")
    @Operation(summary = "Request OTP", description = "Request OTP to reset password")
    public ResponseEntity<Object> requestOtp(@RequestParam String email) {
        String detail = authenticationService.sendOtpToEmail(email);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Đã gửi mã OTP đến email: " + email,
                "detail", detail
        ));
    }

    @PostMapping("/forgot-password/verify")
    @Operation(summary = "Verify OTP", description = "Verify OTP to reset password")
    public ResponseEntity<Object> verifyOtp(@RequestParam String email,
                                            @RequestParam String otp) {
        UUID sessionId = authenticationService.verifyOtpAndCreateSession(email, otp);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Xác thực OTP thành công",
                "sessionId", sessionId
        ));
    }

    @PostMapping("/forgot-password/reset")
    @Operation(summary = "Reset Password", description = "Reset password with sessionId")
    public ResponseEntity<Object> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPasswordWithSession(request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Đổi mật khẩu thành công"
        ));
    }

}
