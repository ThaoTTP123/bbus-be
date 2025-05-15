package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Attendance;
import com.fpt.bbusbe.model.enums.BusDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Attendance findByStudent_IdAndDateAndDirection(UUID studentId, LocalDate date, BusDirection direction);

//    List<Attendance> findAllByStudent_Checkpoint_Route_BusSchedules_Bus_IdAndDirection(UUID studentCheckpointRouteBusSchedulesBusId, BusDirection direction);

    List<Attendance> findAllByStudent_Checkpoint_Route_BusSchedules_Bus_IdAndDirectionAndDate(UUID studentCheckpointRouteBusSchedulesBusId, BusDirection direction, LocalDate date);

//    List<Attendance> findAllByBusIdAndDateAndDirection(UUID busId, LocalDate date, BusDirection direction);

    void deleteByDate(LocalDate date);

    List<Attendance> findAllByBusIdAndDateAndDirectionOrderByStudent_Checkpoint_Name(UUID busId, LocalDate date, BusDirection direction);


    List<Attendance> findAllByStudent_IdAndDateAfter(UUID studentId, LocalDate dateAfter);

//    @Query(value = "SELECT * FROM tbl")
//    List<Object[]> findAllByStudentIdReturnWithDriverNameAndAssistant(UUID studentId);

    @Query(value = """
            SELECT a.id,
                   a.direction,
                   a.status,
                   r.description routeDescription,
                   a.date,
                   a.checkin,
                   a.checkout,
                   d.name        driverName,
                   ass.name      assistantName,
                   c.name        checkpointName,
                   a.modified_by,
                   r.code 
            FROM tbl_attendance a
                     LEFT JOIN tbl_bus_schedule bs ON a.bus_id = bs.bus_id AND a.date = bs.date AND a.direction = bs.direction
                     LEFT JOIN tbl_student s ON a.student_id = s.id
                     LEFT JOIN (SELECT d1.id, u1.name
                                FROM tbl_driver d1
                                         JOIN tbl_user u1 ON d1.user_id = u1.id) d ON bs.driver_id = d.id
                     LEFT JOIN (SELECT ass1.id, u1.name
                                FROM tbl_assistant ass1
                                         JOIN tbl_user u1 ON ass1.user_id = u1.id) ass ON bs.assistant_id = ass.id
                     LEFT JOIN tbl_route r ON bs.route_id = r.id
                     LEFT JOIN tbl_checkpoint c ON a.checkpoint_id = c.id
            WHERE a.student_id = :studentId
              AND a.date <= current_date
            ORDER BY a.date DESC;
            """, nativeQuery = true)
    List<Object[]> findAllByStudentIdReturnWithDriverNameAndAssistant(UUID studentId);

    @Query("SELECT a FROM Attendance a " +
            "WHERE a.bus.id = :busId " +
            "AND a.date = :date " +
            "AND a.direction = :direction ")
    List<Attendance> findAllByBusSchedule(UUID busId, LocalDate date, BusDirection direction);

    List<Attendance> findAllByStudent_IdAndDate(UUID studentId, LocalDate date);

    @Query(value = """
            SELECT grade,
                   count(grade)                                    amountOfStudentRegistered,
                   count(CASE WHEN status = 'INACTIVE' THEN 1 END) amountOfStudentDeregistered
            FROM (SELECT left(s.class_name, 1) grade, s.status
                  FROM tbl_student s
                           JOIN (SELECT a.student_id, min(a.date)
                                 FROM tbl_attendance a
                                 GROUP BY a.student_id) registered ON registered.student_id = s.id) all_info
            GROUP BY grade
            ORDER BY grade
            """, nativeQuery = true)
    List<Object[]> finalReport();

    @Query(value = """
            SELECT b.license_plate,
                   d.name,
                   a.name,
                   amountOfRide,
                   b.max_capacity
            FROM tbl_bus b
                     JOIN (SELECT bs.bus_id, bs.assistant_id, bs.driver_id, count(*) amountOfRide
                           FROM tbl_bus_schedule bs
                           GROUP BY bs.bus_id, bs.assistant_id, bs.driver_id) bs ON b.id = bs.bus_id
                     JOIN (SELECT a.id, u.name
                           FROM tbl_assistant a
                                    JOIN tbl_user u ON a.user_id = u.id) a ON a.id = b.assistant_id
                     JOIN (SELECT d.id, u.name
                           FROM tbl_driver d
                                    JOIN tbl_user u ON d.user_id = u.id) d ON b.driver_id = d.id;
            """, nativeQuery = true)
    List<Object[]> busReport();

    @Query(value = """
            WITH route_path AS (
                SELECT
                    r.id,
                    r.description AS routeName,
                    string_agg(c.name, ' -> ' ORDER BY t.ord) AS pathName
                FROM tbl_route r
                         JOIN unnest(string_to_array(r.path, ' ')) WITH ORDINALITY AS t(checkpoint_id, ord)
                              ON TRUE
                         JOIN tbl_checkpoint c ON c.id::text = t.checkpoint_id
                GROUP BY r.id, r.description
            ),
                 student_count AS (
                     SELECT
                         r.id,
                         COUNT(CASE WHEN s.status = 'ACTIVE' THEN 1 END) AS amountOfStudent
                     FROM tbl_student s
                              JOIN tbl_checkpoint c ON s.checkpoint_id = c.id
                              JOIN tbl_route r ON c.route_id = r.id
                     GROUP BY r.id
                 ),
                 trip_count AS (
                     SELECT
                         r.id,
                         COUNT(*) AS amountOfTrip
                     FROM tbl_bus_schedule bs
                              JOIN tbl_route r ON bs.route_id = r.id
                     GROUP BY r.id
                 )
            SELECT
                rp.routeName,
                rp.pathName,
                sc.amountOfStudent,
                tc.amountOfTrip
            FROM route_path rp
                     JOIN student_count sc ON rp.id = sc.id
                     JOIN trip_count tc ON rp.id = tc.id;
            """, nativeQuery = true)
    List<Object[]> routeReport();

    @Query(value = """
            SELECT s.name,
                   s.roll_number,
                   COUNT(CASE WHEN a.date IS NOT NULL AND a.checkin IS NOT NULL THEN 1 END)     checkInNumber,
                   COUNT(CASE WHEN a.date IS NOT NULL AND a.checkout IS NOT NULL THEN 1 END)    checkOutNumber,
                   COUNT(CASE WHEN a.date IS NOT NULL AND a.checkin IS NOT NULL THEN 1 END) +
                   COUNT(CASE WHEN a.date IS NOT NULL AND a.checkout IS NOT NULL THEN 1 END) AS totalCheckInOut,
                   CASE
                       WHEN COUNT(CASE WHEN a.date IS NOT NULL AND a.checkin IS NOT NULL THEN 1 END) !=
                            COUNT(CASE WHEN a.date IS NOT NULL AND a.checkout IS NOT NULL THEN 1 END) THEN 'Có vấn đề'
                       ELSE 'Đi học đều' END                                                 AS note
            FROM tbl_attendance a
                     JOIN tbl_student s ON a.student_id = s.id
            GROUP BY s.name, s.roll_number;
            
            """, nativeQuery = true)
    List<Object[]> attendanceReport();

    @Query(value = """
            SELECT d.name   AS driver_name,
                   ast.name AS assistant_name,
                   ast.numberOfManualAttendance
            FROM (SELECT ast.id,
                         u.name,
                         COUNT(*) AS numberOfManualAttendance
                  FROM tbl_assistant ast
                           JOIN tbl_user u ON ast.user_id = u.id
                           LEFT JOIN tbl_attendance atd ON atd.modified_by = ast.id::varchar
                  GROUP BY ast.id, u.name) ast
                     JOIN tbl_bus b ON ast.id = b.assistant_id
                     JOIN (SELECT d.id,
                                  u.name
                           FROM tbl_driver d
                                    JOIN tbl_user u ON d.user_id = u.id) d ON b.driver_id = d.id;
            """, nativeQuery = true)
    List<Object[]> driverAndAssistantReport();

    @Query(value = """
            WITH month_series AS (
                    SELECT TO_CHAR(generate_series(:startDate, :endDate, interval '1 month'), 'YYYY-MM') AS month
                ),
                attendance_summary AS (
                    SELECT
                        TO_CHAR(date, 'YYYY-MM') AS month,
                        COUNT(*) FILTER (
                            WHERE modified_by IS NOT NULL
                              AND modified_by !~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
                        ) AS non_uuid_count,
                        COUNT(*) FILTER (WHERE modified_by IS NOT NULL) AS total_modified
                    FROM tbl_attendance
                    WHERE date BETWEEN :startDate AND :endDate
                    GROUP BY TO_CHAR(date, 'YYYY-MM')
                )
                SELECT
                    m.month,
                    ROUND(100.0 * COALESCE(a.non_uuid_count, 0) / NULLIF(a.total_modified, 0), 2) AS non_uuid_percentage
                FROM month_series m
                LEFT JOIN attendance_summary a ON m.month = a.month
                ORDER BY m.month
            """, nativeQuery = true)
    List<Object[]> findAttendanceRate(    @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}
