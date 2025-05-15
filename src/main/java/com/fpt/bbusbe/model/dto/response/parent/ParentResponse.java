package com.fpt.bbusbe.model.dto.response.parent;

import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ParentResponse {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String avatar;
    private Gender gender;
    private Date dob;
    private UserStatus status;
}
