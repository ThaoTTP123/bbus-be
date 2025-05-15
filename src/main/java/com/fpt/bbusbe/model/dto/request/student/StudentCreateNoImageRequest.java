package com.fpt.bbusbe.model.dto.request.student;

import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StudentCreateNoImageRequest {
    @NotBlank(message = "Roll number must not be blank")
    private String rollNumber;

    @NotBlank(message = "name must not be blank")
    private String name;

    private String className;

    private String avatar;

    private Date dob;

    private String address;

    private Gender gender;

    private StudentStatus status;

    private UUID parentId;
}
