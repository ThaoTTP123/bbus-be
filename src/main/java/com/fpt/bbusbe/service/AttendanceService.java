package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.dto.db.AttendanceDTO;
import com.fpt.bbusbe.model.dto.request.attendance.ManualAttendanceRequest;
import com.fpt.bbusbe.model.dto.response.attendance.*;
import com.fpt.bbusbe.model.dto.response.bus.BusReport;
import com.fpt.bbusbe.model.dto.response.route.RouteReport;
import com.fpt.bbusbe.model.entity.Attendance;
import com.fpt.bbusbe.model.enums.BusDirection;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
//    List<Attendance> findAllByStudent_Checkpoint_Route_BusSchedules_Bus_IdAndDirection(UUID busId, BusDirection direction);

    AttendanceDTO convertToDTO(Attendance attendance);

    List<AttendanceResponse> findAllByBusIdAndDateAndDirection(UUID busId, LocalDate date, BusDirection direction);

    List<AttendanceResponseForAdmin> getAttendanceHistoryOfAStudent(UUID studentId);

    void manualAttendance(ManualAttendanceRequest req);

    List<AttendanceResponse> getAttendanceHistoryOfAStudentForParent(UUID studentId, LocalDate date);

    List<FinalReport> generateFinalReport();

    List<BusReport> generateBusReport();

    List<RouteReport> generateRouteReport();

    List<AttendanceReport> generateAttendanceReport();

    List<DriverAndAssistantReport> generateDriverAndAssistantReport();

    DashBoard dashboard();
}
