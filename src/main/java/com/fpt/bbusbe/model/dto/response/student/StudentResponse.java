package com.fpt.bbusbe.model.dto.response.student;

import com.fpt.bbusbe.model.dto.response.bus.BusResponse;
import com.fpt.bbusbe.model.dto.response.user.UserResponse;
import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.StudentStatus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@Data
public class StudentResponse {
    private UUID id;
    private String rollNumber;
    private String name;
    private String className;
    private String avatar;
    private Date dob;
    private String address;
    private Gender gender;
    private StudentStatus status;
    private UUID parentId;
    private UUID busId;
    private String busName;
    private UserResponse parent;
    private UUID checkpointId;
    private String checkpointName;
    private String checkpointDescription;
    private Date createdAt;
    private Date updatedAt;
}
