package com.fpt.bbusbe.model.dto.db;

import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusDirection;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceFirebaseDto {
    private UUID studentId;
    private String studentName;
    private String time;
    private AttendanceStatus status;
    private BusDirection direction;
    private String pic;
    private String modifiedBy;
}
