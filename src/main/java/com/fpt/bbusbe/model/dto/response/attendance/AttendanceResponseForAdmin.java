package com.fpt.bbusbe.model.dto.response.attendance;

import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusDirection;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponseForAdmin {
    private UUID id;
    private String driverName;
    private String assistantName;
    private LocalDate date;
    private String routeCode;
    private String routeDescription;
    private BusDirection direction;
    private AttendanceStatus status;
    private Date checkin;
    private Date checkout;
    private String checkpointName;
    private String modifiedBy;

}
