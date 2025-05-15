package com.fpt.bbusbe.model.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SignInRequest implements Serializable {

    @NotBlank(message = "Phone number is required")
    private String phone;
    @NotBlank(message = "Password is required")
    private String password;
    private String platform;        //web, mobile
    private String deviceToken;     // insert vào db để có các action push noti nào thì sẽ push theo deviceToken để đến được app đấy
    private String versionApp;

}
