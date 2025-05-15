package com.fpt.bbusbe.model.dto.request.student;

import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@ToString
public class StudentUpdateRequest {

    private UUID id;

    private String rollNumber;

    private String className;

    private String name;

    private Date dob;

    private String address;

    private Gender gender;

    private UUID parentId;

    private UUID checkpointId;
}
