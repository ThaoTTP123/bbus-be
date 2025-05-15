package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Request;
import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.model.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    @Query("SELECT r FROM Request r " +
            "WHERE (lower(r.requestType.requestTypeName) LIKE :keyword " +
            "OR lower(CAST(r.status AS string)) LIKE :keyword) ")
    Page<Request> searchByKeyword(@Param("keyword") String keyword,
                                     Pageable pageable);


    List<Request> findAllBySendBy_Id(UUID sendById);

    List<Request> findAllBySendBy_IdAndRequestType_IdOrderByCreatedAtDesc(UUID sendById, UUID requestTypeId);

    List<Request> findAllBySendBy_IdOrderByCreatedAtDesc(UUID sendById);

    long countByStatus(RequestStatus status);
}