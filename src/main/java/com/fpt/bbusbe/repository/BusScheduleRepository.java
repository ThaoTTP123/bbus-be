package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.BusSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface BusScheduleRepository extends JpaRepository<BusSchedule, UUID> {
    List<BusSchedule> findByDateAndDriver_User_Id(LocalDate date, UUID driverUserId);

    List<BusSchedule> findByDateAndAssistant_User_Id(LocalDate date, UUID assistantUserId);

//    @Query("SELECT bs FROM BusSchedule bs WHERE FUNCTION('MONTH', bs.date) = :month AND FUNCTION('YEAR', bs.date) = :year")
//    Page<BusSchedule> findByMonthAndYear(@Param("month") int month, @Param("year") int year, Pageable pageable);

    @Query("SELECT bs FROM BusSchedule bs WHERE bs.date BETWEEN :start AND :end")
    Page<BusSchedule> findByDateBetween(@Param("start") LocalDate start,
                                        @Param("end") LocalDate end,
                                        Pageable pageable);

    @Query("SELECT DISTINCT bs.date FROM BusSchedule bs WHERE bs.date BETWEEN :start AND :end ORDER BY bs.date ASC")
    List<LocalDate> findDistinctDatesByMonth(@Param("start") LocalDate start,
                                             @Param("end") LocalDate end);

    void deleteByDate(LocalDate date);

    List<BusSchedule> findByDateBetweenAndAssistant_User_Id(LocalDate startDate, LocalDate endDate, UUID assistantId);

    @Query("SELECT b FROM BusSchedule b WHERE b.assistant.user.id = :assistantId AND b.date BETWEEN :startDate AND :endDate AND b.direction = 'PICK_UP'")
    List<BusSchedule> findPickUpScheduleByMonth(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("assistantId") UUID assistantId
    );

    @Query("SELECT b FROM BusSchedule b WHERE b.driver.user.id = :driverId AND b.date BETWEEN :startDate AND :endDate AND b.direction = 'PICK_UP'")
    List<BusSchedule> findPickUpScheduleByMonthForDriver(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("driverId") UUID driverId
    );

}