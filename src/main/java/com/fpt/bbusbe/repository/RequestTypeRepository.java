package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Request;
import com.fpt.bbusbe.model.entity.RequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestTypeRepository extends JpaRepository<RequestType, UUID> {

    @Query("SELECT rt FROM RequestType rt " +
            "WHERE (lower(rt.requestTypeName) LIKE :keyword ) ")
    Page<RequestType> searchByKeyword(@Param("keyword") String keyword,
                                  Pageable pageable);
}
