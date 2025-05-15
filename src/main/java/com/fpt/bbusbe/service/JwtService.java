package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.enums.TokenType;

import java.util.List;
import java.util.UUID;

public interface JwtService {

    String generateAccessToken(String phone, UUID userId, List<String> authorities);

    String generateRefreshToken(String phone, UUID userId, List<String> authorities);

    String extractPhone(String token, TokenType type);
}
