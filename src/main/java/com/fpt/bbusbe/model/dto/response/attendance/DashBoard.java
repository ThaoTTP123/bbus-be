package com.fpt.bbusbe.model.dto.response.attendance;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashBoard {
    private long totalActiveStudent;
    private long totalActiveRoute;
    private long totalActiveUser;
    private List<AttendanceRate> attendanceRate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceRate {
        private BigDecimal percentage;
        private String month;
    }
}
