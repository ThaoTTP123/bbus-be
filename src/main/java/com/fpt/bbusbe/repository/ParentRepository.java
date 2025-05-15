package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Parent;
import com.fpt.bbusbe.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParentRepository extends JpaRepository<Parent, UUID> {

    @Query("SELECT p FROM Parent p JOIN User u ON p.user.id = u.id WHERE u.phone = :phone")
    Optional<Parent> findParentIdByPhone(@Param("phone") String phone);

    Object findByUser(User user);

    @Query("SELECT p FROM Parent p JOIN User u ON p.user.id = u.id WHERE u.id = :id")
    Parent findByUserId(UUID id);

    @Query("SELECT p FROM User u " +
            "JOIN Parent p ON u.id = p.user.id " +
            "WHERE (lower(u.name) LIKE :keyword " +
            "OR lower(CAST(u.gender AS string)) LIKE :keyword " +
            "OR lower(u.username) LIKE :keyword " +
            "OR lower(u.phone) LIKE :keyword " +
            "OR lower(u.email) LIKE :keyword) ")
    Page<Parent> searchByKeyword(@Param("keyword") String keyword,
                               Pageable pageable);

    @Query("""
                SELECT DISTINCT p
                FROM Parent p
                JOIN FETCH p.user u
            """)
    List<Parent> findAllCustom();
}
