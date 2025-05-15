package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Assistant;
import com.fpt.bbusbe.model.entity.Assistant;
import com.fpt.bbusbe.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssistantRepository extends JpaRepository<Assistant, UUID> {
    Assistant findByUser_Phone(String phone);
    Object findByUser(User user);


    @Query("SELECT a FROM User u " +
            "JOIN Assistant a ON u.id = a.user.id " +
            "WHERE (lower(u.name) LIKE :keyword " +
            "OR lower(CAST(u.gender AS string)) LIKE :keyword " +
            "OR lower(u.username) LIKE :keyword " +
            "OR lower(u.phone) LIKE :keyword " +
            "OR lower(u.email) LIKE :keyword) ")
    Page<Assistant> searchByKeyword(@Param("keyword") String keyword,
                                 Pageable pageable);

    @Query("SELECT a FROM Assistant a WHERE a.id NOT IN " +
            "(SELECT bs.assistant.id FROM BusSchedule bs WHERE bs.assistant IS NOT NULL) " +
            "AND (LOWER(a.user.name) LIKE :keyword OR LOWER(a.user.phone) LIKE :keyword)")
    Page<Assistant> findAvailableAssistants(@Param("keyword") String keyword, Pageable pageable);


}
