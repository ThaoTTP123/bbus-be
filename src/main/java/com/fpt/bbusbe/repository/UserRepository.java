package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.model.enums.UserStatus;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u " +
            "JOIN UserHasRole ur ON u.id = ur.user.id " +
            "JOIN Role r ON ur.role.id = r.id " +
            "WHERE (lower(u.name) LIKE :keyword " +
            "OR lower(CAST(u.gender AS string)) LIKE :keyword " +
            "OR lower(u.username) LIKE :keyword " +
            "OR lower(u.phone) LIKE :keyword " +
            "OR lower(u.email) LIKE :keyword) ")
    Page<User> searchByKeyword(@Param("keyword") String keyword,
                               Pageable pageable);


    @Query("SELECT u FROM User u " +
            "JOIN UserHasRole ur ON u.id = ur.user.id " +
            "JOIN Role r ON ur.role.id = r.id " +
            "WHERE (lower(u.name) LIKE :keyword " +
            "OR lower(CAST(u.gender AS string)) LIKE :keyword " +
            "OR lower(u.username) LIKE :keyword " +
            "OR lower(u.phone) LIKE :keyword " +
            "OR lower(u.email) LIKE :keyword) " +
            "AND lower(r.name) = lower(:roleName)")
    Page<User> searchByKeywordAndRole(@Param("keyword") String keyword,
                                      @Param("roleName") String roleName,
                                      Pageable pageable);

//    User findByUsername(String username);

    User findByPhone(String phone);

    User findByEmail(String email);

//    @Query("SELECT u FROM User u " +
//            "JOIN UserHasRole ur ON u.id = ur.user.id " +
//            "JOIN Role r ON ur.role.id = r.id " +
//            "WHERE lower(u.phone) LIKE :phone " +
//            "AND lower(r.name) = lower(:busId)")
//    List<User> findUserCanTrackBus(@Param("phone")String phone,
//                                      @Param("busId")UUID busId);

//    @Query("SELECT u FROM User u " +
//            "JOIN Parent p ON u.id = p.user.id " +
//            "JOIN Student s ON s.parent.id = p.id " +
//            "WHERE s.rollNumber = :rollNumber ")
//    User findParentByStudentRollNumber(@Param("rollNumber") String rollNumber);

    List<User> findByEmailOrPhoneOrId(String email, @Pattern(regexp = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$",
            message = "Invalid phone number") String phone, UUID id);

    long countByStatus(UserStatus status);
}
