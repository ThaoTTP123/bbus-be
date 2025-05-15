package com.fpt.bbusbe.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_password_reset_session")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetSession extends AbstractEntity {

    @Column(name = "email")
    private String email;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;
}
