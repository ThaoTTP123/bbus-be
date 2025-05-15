package com.fpt.bbusbe.model.dto.request.student;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class StudentUpdateByParentRequest {
    @NotNull(message = "Student ID là bắt buộc")
    private UUID studentId;

    private String name;
    private Date dob;
    private String address;
}
