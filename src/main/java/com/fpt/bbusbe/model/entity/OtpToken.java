package com.fpt.bbusbe.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_otp_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpToken extends AbstractEntity {

    @Column(name = "email")
    private String email;

    @Column(name = "otp")
    private String otp;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;
}
