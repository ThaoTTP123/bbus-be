package com.fpt.bbusbe.model.dto.request.student;

import com.fpt.bbusbe.model.enums.StudentStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StudentUpdateStatusRequest {
    private UUID id;
    private StudentStatus status;
}
