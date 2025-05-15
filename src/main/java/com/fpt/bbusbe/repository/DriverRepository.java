package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Driver;
import com.fpt.bbusbe.model.entity.Parent;
import com.fpt.bbusbe.model.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Driver findByUser_Phone(String driverPhone);

    Object findByUser(User user);

    @Query("SELECT d FROM User u " +
            "JOIN Driver d ON u.id = d.user.id " +
            "WHERE (lower(u.name) LIKE :keyword " +
            "OR lower(CAST(u.gender AS string)) LIKE :keyword " +
            "OR lower(u.username) LIKE :keyword " +
            "OR lower(u.phone) LIKE :keyword " +
            "OR lower(u.email) LIKE :keyword) ")
    Page<Driver> searchByKeyword(@Param("keyword") String keyword,
                                 Pageable pageable);

    @Query("SELECT d FROM Driver d WHERE d.id NOT IN " +
            "(SELECT bs.driver.id FROM BusSchedule bs WHERE bs.driver IS NOT NULL) " +
            "AND (LOWER(d.user.name) LIKE :keyword OR LOWER(d.user.phone) LIKE :keyword)")
    Page<Driver> findAvailableDrivers(@Param("keyword") String keyword, Pageable pageable);

}
