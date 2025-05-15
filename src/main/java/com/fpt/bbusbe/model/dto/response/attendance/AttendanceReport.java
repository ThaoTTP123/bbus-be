package com.fpt.bbusbe.model.dto.response.attendance;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceReport {
    private String studentName;
    private String rollNumber;
    private long checkInNumber;
    private long checkOutNumber;
    private long totalCheckNumber;
    private String note;
}
