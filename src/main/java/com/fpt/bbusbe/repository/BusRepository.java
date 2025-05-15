package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Bus;
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
public interface BusRepository extends JpaRepository<Bus, UUID> {

    @Query("SELECT b FROM Bus b " +
            "WHERE (lower(b.name)) LIKE :keyword " +
            "OR lower(b.licensePlate) LIKE :keyword " +
            "OR lower(CAST(b.status AS string)) LIKE :keyword " )
    Page<Bus> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Bus findByEspId(String espId);

    List<Bus> findAllByRoute_Id(UUID routeId);

    Bus findByCamera_Facesluice(String cameraFacesluice);

    Bus findByName(String name);

    @Query("SELECT b FROM Bus b WHERE b.route.id = (SELECT c.route.id FROM Checkpoint c WHERE c.id = :checkpointId)")
    List<Bus> findAllByCheckpointId(@Param("checkpointId") UUID checkpointId);

    @Query("SELECT b FROM Bus b WHERE b.name LIKE 'Bus %' ORDER BY b.name DESC LIMIT 1")
    Optional<Bus> findTopByNameOrderByNameDesc();


}
