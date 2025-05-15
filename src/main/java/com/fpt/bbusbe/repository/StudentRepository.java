package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Student;
import com.fpt.bbusbe.model.enums.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    @EntityGraph(attributePaths = {
            "parent",
            "parent.user",
            "checkpoint",
            "bus"
    })
    @Query(value = "select u from Student u where " +
            "(lower(u.name) like :keyword " +
            "or lower(u.address) like :keyword " +
            "or lower(u.parent.user.name) like :keyword " +
            "or lower(u.parent.user.phone) like :keyword)")
    Page<Student> searchByKeyword(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {
            "parent",
            "parent.user",
            "checkpoint",
            "bus"
    })
    @Query("SELECT s FROM Student s")
    Page<Student> findAllStudent(Pageable pageable);

    @Query("SELECT MAX(s.rollNumber) FROM Student s WHERE s.rollNumber LIKE 'HS%'")
    String findMaxRollNumber();

    Student findByRollNumber(String rollNumber);

    List<Student> findByParent_User_Id(UUID parentUserId);

    boolean existsByRollNumber(String rollNumber);

//    Student findByIdAndParent_User_Id(UUID id, UUID parentUserId);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.checkpoint.id = :checkpointId")
    int countStudentsByCheckpointId(@Param("checkpointId") UUID checkpointId);

    @Query(value = """
            SELECT s.id                                                          studentId,
                   s.roll_number                                                 rollNumber,
                   s.name                                                        studentName,
                   s.gender,
                   s.address,
                   p.name                                                        parentName,
                   cp.name,
                   CASE WHEN s.avatar = crd.avatar THEN crd.err_code ELSE -1 END errCode
            --SELECT *
            FROM tbl_student s
                     JOIN tbl_bus b ON s.bus_id = b.id AND b.id = :busId
                     JOIN tbl_camera c ON b.id = c.bus_id
                     JOIN (SELECT p.id, u.name
                           FROM tbl_parent p
                                    JOIN tbl_user u ON p.user_id = u.id) p ON p.id = s.parent_id
                     JOIN tbl_checkpoint cp ON s.checkpoint_id = cp.id
                     LEFT JOIN (SELECT crd2.*, cr2.created_at, cr2.camera_id
                                FROM (SELECT crd1.student_id, max(cr1.created_at) lastest
                                      FROM tbl_camera_request_detail crd1
                                               JOIN tbl_camera_request cr1 ON crd1.camera_request_id = cr1.id
                                      GROUP BY student_id) l
                                         JOIN tbl_camera_request cr2 ON l.lastest = cr2.created_at
                                         JOIN tbl_camera_request_detail crd2
                                              ON cr2.id = crd2.camera_request_id AND crd2.student_id = l.student_id) crd
                               ON s.id = crd.student_id AND c.facesluice = crd.camera_id;
            """, nativeQuery = true)
    List<Object[]> findByBus_Id(@Param("busId") UUID busId);

    List<Student> findByParent_Id(UUID parentId);

    long countByCheckpoint_Id(UUID checkpointId);

    boolean existsByCheckpoint_Id(UUID checkpointId);

    long countByStatus(StudentStatus status);
}
