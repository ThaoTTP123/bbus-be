package com.fpt.bbusbe.service;


import com.fpt.bbusbe.model.dto.request.auth.ResetPasswordRequest;
import com.fpt.bbusbe.model.dto.request.auth.SignInRequest;
import com.fpt.bbusbe.model.dto.response.auth.LoginResponse;
import com.fpt.bbusbe.model.dto.response.auth.TokenResponse;

import java.util.UUID;

public interface AuthenticationService {

    LoginResponse login(SignInRequest signInRequest);
    TokenResponse getRefreshToken(String refreshToken);

    String sendOtpToEmail(String email);
    UUID verifyOtpAndCreateSession(String email, String otp);
    void resetPasswordWithSession(ResetPasswordRequest request);


}
