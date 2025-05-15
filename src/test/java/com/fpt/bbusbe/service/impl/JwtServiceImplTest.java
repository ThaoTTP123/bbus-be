package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.model.enums.TokenType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtServiceImpl();

        // Use reflection to set private variables
        setPrivateField(jwtService, "expiryMinutes", 15L);
        setPrivateField(jwtService, "expiryDay", 7L);
        setPrivateField(jwtService, "accessKey", "testAccessKey12345678901234567890123456789012");
        setPrivateField(jwtService, "refreshKey", "testRefreshKey12345678901234567890123456789012");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testGenerateAccessToken() {
        String token = jwtService.generateAccessToken("123456789", UUID.randomUUID(), List.of("ROLE_USER"));
        assertNotNull(token);
    }

    @Test
    void testGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken("123456789", UUID.randomUUID(), List.of("ROLE_USER"));
        assertNotNull(token);
    }

    @Test
    void testExtractPhone_Success() {
        String token = jwtService.generateAccessToken("123456789", UUID.randomUUID(), List.of("ROLE_USER"));
        String phone = jwtService.extractPhone(token, TokenType.ACCESS_TOKEN);
        assertEquals("123456789", phone);
    }

    @Test
    void testExtractPhone_InvalidToken() {
        assertThrows(AccessDeniedException.class, () -> jwtService.extractPhone("invalidToken", TokenType.ACCESS_TOKEN));
    }

    @Test
    void testGetKey_InvalidTokenType() {
        assertThrows(InvalidDataException.class, () -> jwtService.extractPhone("token", null));
    }

    @Test
    void testExtraAllClaim_ExpiredToken() {
        try (MockedStatic<io.jsonwebtoken.Jwts> mockedJwts = Mockito.mockStatic(io.jsonwebtoken.Jwts.class)) {
            mockedJwts.when(() -> io.jsonwebtoken.Jwts.parserBuilder().build().parseClaimsJws(anyString()))
                    .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

            assertThrows(AccessDeniedException.class, () -> jwtService.extractPhone("expiredToken", TokenType.ACCESS_TOKEN));
        }
    }

    @Test
    void testExtraAllClaim_InvalidSignature() {
        try (MockedStatic<io.jsonwebtoken.Jwts> mockedJwts = Mockito.mockStatic(io.jsonwebtoken.Jwts.class)) {
            mockedJwts.when(() -> io.jsonwebtoken.Jwts.parserBuilder().build().parseClaimsJws(anyString()))
                    .thenThrow(new SignatureException("Invalid signature"));

            assertThrows(AccessDeniedException.class, () -> jwtService.extractPhone("invalidSignatureToken", TokenType.ACCESS_TOKEN));
        }
    }
}
