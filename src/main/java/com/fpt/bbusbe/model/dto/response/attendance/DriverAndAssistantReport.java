package com.fpt.bbusbe.model.dto.response.attendance;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverAndAssistantReport {
    private String driverName;
    private String assistantName;
    private long numberOfManualAttendance;
}
