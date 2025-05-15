package com.fpt.bbusbe.model.dto.request.user;

import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Getter
@Setter
@ToString
public class UserCreationNoImageRequest {
    @NotBlank(message = "username must not be blank")
    private String username;

    @NotBlank
    private String password;

    @Email(message = "Email is invalid format!")
    private String email;

    @Pattern(regexp = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$",
            message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "name must not be blank")
    private String name;

    private String address;

    private Gender gender;

    private Date dob;

    private String avatar;

    private Role role;
}
