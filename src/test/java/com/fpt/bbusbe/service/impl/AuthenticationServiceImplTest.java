package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ForBiddenException;
import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.model.dto.request.auth.SignInRequest;
import com.fpt.bbusbe.model.dto.response.auth.LoginResponse;
import com.fpt.bbusbe.model.dto.response.auth.TokenResponse;
import com.fpt.bbusbe.repository.UserRepository;
import com.fpt.bbusbe.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setPhone("123456789");
        request.setPassword("password");
        request.setDeviceToken("deviceToken");

        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setPhone("123456789");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(userRepository.findByPhone("123456789")).thenReturn(user);
        when(jwtService.generateAccessToken(anyString(), eq(userId), anyList())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyString(), eq(userId), anyList())).thenReturn("refreshToken");

        // Act
        LoginResponse response = authenticationService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccess_token());
        assertEquals("refreshToken", response.getRefresh_token());
        assertEquals("Login successful", response.getMessage());
        verify(userRepository).save(user);
    }



    @Test
    void testLogin_BadCredentials() {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setPhone("123456789");
        request.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.login(request));
    }

    @Test
    void testGetRefreshToken_Success() {
        // Arrange
        String refreshToken = "validRefreshToken";
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setPhone("123456789");

        when(jwtService.extractPhone(refreshToken, any())).thenReturn("123456789");
        when(userRepository.findByPhone("123456789")).thenReturn(user);
        when(jwtService.generateAccessToken(anyString(), userId, anyList())).thenReturn("newAccessToken");

        // Act
        TokenResponse response = authenticationService.getRefreshToken(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccess_token());
        assertEquals(refreshToken, response.getRefresh_token());
    }

    @Test
    void testGetRefreshToken_InvalidToken() {
        // Arrange
        String refreshToken = "";

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> authenticationService.getRefreshToken(refreshToken));
    }

    @Test
    void testGetRefreshToken_Forbidden() {
        // Arrange
        String refreshToken = "invalidRefreshToken";

        when(jwtService.extractPhone(refreshToken, any())).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        assertThrows(ForBiddenException.class, () -> authenticationService.getRefreshToken(refreshToken));
    }
}
