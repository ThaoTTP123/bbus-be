package com.fpt.bbusbe.model.dto.request.student;

import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StudentCreationRequest implements Serializable {

    @NotBlank(message = "Roll number must not be blank")
    private String rollNumber;

    @NotBlank(message = "name must not be blank")
    private String name;

    private String className;

    private MultipartFile[] avatar;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dob;

    private String address;

    private Gender gender;

    private StudentStatus status;

    private UUID parentId;

}
