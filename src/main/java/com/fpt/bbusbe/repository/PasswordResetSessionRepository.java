package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.PasswordResetSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PasswordResetSessionRepository extends JpaRepository<PasswordResetSession, UUID> {
}

