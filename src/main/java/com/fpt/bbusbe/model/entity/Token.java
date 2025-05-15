package com.fpt.bbusbe.model.entity;

import com.fpt.bbusbe.model.enums.TokenType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "tbl_token")
public class Token extends AbstractEntity {

    @Column(name = "token")
    private String token;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "token_type", length = 255)
    private TokenType tokenType;

    @Column(name = "revoked")
    private boolean revoked;

    @Column(name = "expired")
    private boolean expired;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
