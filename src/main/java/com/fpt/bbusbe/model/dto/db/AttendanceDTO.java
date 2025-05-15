package com.fpt.bbusbe.model.dto.db;

import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusDirection;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDTO {
    private UUID studentId;
    private LocalDate date;
    private BusDirection direction;
    private UUID checkpointId;
    private UUID busId;
    private AttendanceStatus status;
    private Date checkin;
    private Date checkout;
}