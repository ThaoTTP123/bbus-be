package com.fpt.bbusbe.model.dto.response.user;

import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.UserStatus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@Data
public class UserResponse implements Serializable {
    private UUID userId;
    private String username;
    private String name;
    private Gender gender;
    private Date dob;
    private String email;
    private String avatar;
    private String phone;
    private String address;
    private UserStatus status;
//    private List<String> roles;
    private String role;
    private Date createdAt;
    private Date updatedAt;
}
