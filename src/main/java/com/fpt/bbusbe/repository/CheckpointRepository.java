package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.dto.response.checkpoint.CheckpointWithAmountOfStudentResponse;
import com.fpt.bbusbe.model.entity.Checkpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, UUID> {

    //Select all checkpoints with amount of students registered
    @Query("SELECT new com.fpt.bbusbe.model.dto.response.checkpoint.CheckpointWithAmountOfStudentResponse(c.id, c.name, c.description, c.latitude, c.longitude, c.status, COUNT(s.id)) " +
            "FROM Checkpoint c LEFT JOIN Student s ON c.id = s.checkpoint.id " +
            "WHERE (lower(c.name) LIKE :keyword OR lower(c.description) LIKE :keyword) " +
            "GROUP BY c.id")
    Page<CheckpointWithAmountOfStudentResponse> findAllWithAmountOfStudentRegister(@Param("keyword") String keyword,
                                                                                   Pageable pageable);

    @Query("SELECT c FROM Checkpoint c " +
            "WHERE (lower(c.name) LIKE :keyword " +
            "OR lower(c.description) LIKE :keyword) ")
    Page<Checkpoint> searchByKeyword(@Param("keyword") String keyword,
                                     Pageable pageable);

    Checkpoint findByName(String name);

    @Query(value = """
            SELECT p.id, p.name, p.pending, r.registered
                        FROM (
                            SELECT c.id, c.name, count(r.id) as pending
                            FROM tbl_checkpoint c
                            LEFT JOIN tbl_request r ON c.id = r.checkpoint_id AND r.status = 'PENDING'
                            WHERE c.status = 'ACTIVE' AND c.name LIKE :keyword
                            GROUP BY c.id, c.name
                        ) as p
                        JOIN (
                            SELECT c.id, c.name, count(s.id) as registered
                            FROM tbl_checkpoint c
                            LEFT JOIN tbl_student s ON c.id = s.checkpoint_id AND s.status = 'ACTIVE'
                            WHERE c.status = 'ACTIVE' AND c.name LIKE :keyword
                            GROUP BY c.id, c.name
                        ) as r ON p.id = r.id
            """, nativeQuery = true )
    List<Object[]> findAllWithAmountOfStudentRegister(@Param("keyword") String keyword);

    List<Checkpoint> findAllByRoute_Id(UUID routeId);

    @Modifying
    @Query("UPDATE Checkpoint c SET c.route.id = :routeId " +
            "WHERE c.id IN :checkpointIds AND c.id <> :schoolCheckpointId")
    void updateRouteForCheckpoints(@Param("routeId") UUID routeId,
                                   @Param("checkpointIds") List<UUID> checkpointIds,
                                   @Param("schoolCheckpointId") UUID schoolCheckpointId);


    @Query("SELECT c FROM Checkpoint c " +
            "WHERE c.route IS NULL")
    List<Checkpoint> findAllByRouteIsNull();


    @Query("SELECT c FROM Checkpoint c " +
            "WHERE c.route IS NOT NULL")
    List<Checkpoint> findAllByRouteIsNotNull();

    @Query(value = "SELECT * FROM tbl_checkpoint " +
            "WHERE id = ANY(:ids) ORDER BY array_position(:ids, id)", nativeQuery = true)
    List<Checkpoint> findAllByIdInOrder(@Param("ids") UUID[] ids);


//    List<Checkpoint> findAllByRoute_IdOrderByCreatedAtAsc(UUID routeId);
}