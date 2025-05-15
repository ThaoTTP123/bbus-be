package com.fpt.bbusbe.model.dto.response.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fpt.bbusbe.model.entity.Attendance;
import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusDirection;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {
    private UUID id;
    private UUID studentId;
    private String rollNumber;
    private String studentName;
    private Date dob;
    private String avatarUrl;
    private BusDirection direction;
    private AttendanceStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "Asia/Ho_Chi_Minh")
    private Date checkin;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "Asia/Ho_Chi_Minh")
    private Date checkout;
    private UUID checkpointId;
    private String checkpointName;
    private String parentName;
    private String parentPhone;

    public AttendanceResponse(Attendance attendance) {
        this.id = attendance.getId();
        this.studentId = attendance.getStudent() != null ? attendance.getStudent().getId() : null;
        this.studentName = attendance.getStudent() != null ? attendance.getStudent().getName() : null;
        this.dob = attendance.getStudent().getDob();
        this.direction = attendance.getDirection();
        this.status = attendance.getStatus();
        this.checkin = attendance.getCheckin();
        this.checkout = attendance.getCheckout();
        this.checkpointId = attendance.getCheckpoint() != null ? attendance.getCheckpoint().getId() : null;
        this.checkpointName = attendance.getCheckpoint() != null ? attendance.getCheckpoint().getName() : null;
        this.parentName = attendance.getStudent().getParent() != null ? attendance.getStudent().getParent().getUser().getName() : null;
        this.parentPhone = attendance.getStudent().getParent() != null ? attendance.getStudent().getParent().getUser().getPhone() : null;
    }
}
