package com.fpt.bbusbe.model.dto.request.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class ManualAttendanceRequest {
    @NotNull(message = "AttendanceId là bắt buộc")
    private UUID attendanceId;

    private Date checkin;
    private Date checkout;
}
