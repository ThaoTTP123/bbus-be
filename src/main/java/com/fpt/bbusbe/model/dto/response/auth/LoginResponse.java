package com.fpt.bbusbe.model.dto.response.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class LoginResponse implements Serializable {
    String message;
    String access_token;
    String refresh_token;
}
