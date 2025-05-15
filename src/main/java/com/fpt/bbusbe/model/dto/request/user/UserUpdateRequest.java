package com.fpt.bbusbe.model.dto.request.user;

import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@ToString
public class UserUpdateRequest implements Serializable {
//    @NotBlank(message = "userId must be uuid")
    private UUID id;

//    @NotBlank(message = "username must not be blank")
    private String username;

    private String name;

    private Gender gender;

    private Date dob;

    private String email;

    private String phone;

    private String address;

    private UserStatus status;

}
