package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ForBiddenException;
import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.auth.ResetPasswordRequest;
import com.fpt.bbusbe.model.entity.OtpToken;
import com.fpt.bbusbe.model.entity.PasswordResetSession;
import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.model.dto.request.auth.SignInRequest;
import com.fpt.bbusbe.model.dto.response.auth.LoginResponse;
import com.fpt.bbusbe.model.dto.response.auth.TokenResponse;
import com.fpt.bbusbe.repository.OtpTokenRepository;
import com.fpt.bbusbe.repository.PasswordResetSessionRepository;
import com.fpt.bbusbe.repository.UserRepository;
import com.fpt.bbusbe.service.AuthenticationService;
import com.fpt.bbusbe.service.EmailService;
import com.fpt.bbusbe.service.JwtService;
import com.fpt.bbusbe.utils.OtpEmailTemplateBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.fpt.bbusbe.model.enums.TokenType.REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE")
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetSessionRepository passwordResetSessionRepository;
    private final EmailService emailService;

    @Override
    public LoginResponse login(SignInRequest request) {
        log.info("Get access token");

        List<String> authorities = new ArrayList<>();
        try {
            // Thực hiện xác thực với username và password
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword()));

            log.info("isAuthenticated = {}", authenticate.isAuthenticated());
            log.info("Authorities: {}", authenticate.getAuthorities().toString());
            authorities.add(authenticate.getAuthorities().toString());

            // Nếu xác thực thành công, lưu thông tin vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authenticate);
        } catch (BadCredentialsException | DisabledException e) {
            log.error("errorMessage: {}", e.getMessage());
            throw new InternalAuthenticationServiceException(e.getMessage());
        }

        User user = userRepository.findByPhone(request.getPhone());
        user.setDeviceToken(request.getDeviceToken());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(request.getPhone(), user.getId(), authorities);
        String refreshToken = jwtService.generateRefreshToken(request.getPhone(), user.getId(), authorities);
        String message = "Login successful";

        return LoginResponse.builder().access_token(accessToken).refresh_token(refreshToken).message(message).build();
    }

    @Override
    public TokenResponse getRefreshToken(String refreshToken) {
        log.info("Get refresh token");

        if (!StringUtils.hasLength(refreshToken)) {
            throw new InvalidDataException("Token must be not blank");
        }

        try {
            // Verify token
            String phone = jwtService.extractPhone(refreshToken, REFRESH_TOKEN);

            // check user is active or inactivated
            User user = userRepository.findByPhone(phone);
            List<String> authorities = new ArrayList<>();
            user.getAuthorities().forEach(authority -> authorities.add(authority.getAuthority()));

            // generate new access token
            String accessToken = jwtService.generateAccessToken(user.getPhone(), user.getId(), authorities);

            return TokenResponse.builder().access_token(accessToken).refresh_token(refreshToken).build();
        } catch (Exception e) {
            log.error("Access denied! errorMessage: {}", e.getMessage());
            throw new ForBiddenException(e.getMessage());
        }
    }

    // Luồng xử lý: Quên mật khẩu -> gửi OTP về số điện thoại -> xác thực OTP -> đổi mật khẩu
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOtpToEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new ResourceNotFoundException("Email không tồn tại trong hệ thống.");

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(5);

        otpTokenRepository.deleteByEmail(email);
        otpTokenRepository.save(OtpToken.builder()
                .email(email)
                .otp(otp)
                .expireAt(expireAt)
                .build());

        String htmlContent = OtpEmailTemplateBuilder.buildOtpEmail(
                otp,
                "BBUS - Yêu cầu đặt lại mật khẩu",
                "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này."
        );

        emailService.sendEmail(email, "BBUS - QUÊN MẬT KHẨU", htmlContent);

        return "Đã gửi mã OTP (" + otp + ") đến email: " + email;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UUID verifyOtpAndCreateSession(String email, String otp) {
        OtpToken token = otpTokenRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy mã OTP"));

        if (!token.getOtp().equals(otp)) throw new InvalidDataException("Mã OTP không đúng");
        if (token.getExpireAt().isBefore(LocalDateTime.now())) throw new InvalidDataException("Mã OTP đã hết hạn");

        otpTokenRepository.delete(token);

        PasswordResetSession session = passwordResetSessionRepository.save(
                PasswordResetSession.builder()
                        .email(email)
                        .expireAt(LocalDateTime.now().plusMinutes(10))
                        .build()
        );

        return session.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordWithSession(ResetPasswordRequest request) {
        PasswordResetSession session = passwordResetSessionRepository.findById(
                        UUID.fromString(request.getSessionId()))
                .orElseThrow(() -> new InvalidDataException("Phiên đặt lại mật khẩu không hợp lệ"));

        if (session.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new InvalidDataException("Phiên đã hết hạn");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("Xác nhận mật khẩu không khớp");
        }

        User user = userRepository.findByEmail(session.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        passwordResetSessionRepository.delete(session);
    }

}
