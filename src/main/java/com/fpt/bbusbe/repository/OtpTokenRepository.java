package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {
    Optional<OtpToken> findByEmail(String email);
    void deleteByEmail(String email);
}

