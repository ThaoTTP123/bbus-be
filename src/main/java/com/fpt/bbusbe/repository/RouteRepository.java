package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Route;
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
public interface RouteRepository extends JpaRepository<Route, UUID> {

    @Query("SELECT r FROM Route r " +
           "WHERE lower(r.code) LIKE %:keyword% " +
           "OR lower(r.description) LIKE %:keyword%")
    Page<Route> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Optional<Route> findByCode(String code);

    Optional<Route> findTopByOrderByCodeDesc();

    Optional<Route> findByPath(String path);

    @Query("SELECT r FROM Route r")
    List<Route> findAllRoutes();

}