package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.CameraRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CameraRequestDetailRepository extends JpaRepository<CameraRequestDetail, CameraRequestDetail.CameraRequestDetailId> {
    @Query(value = """
            SELECT s.id studentId, s.avatar dbAvatar, crd.created_at crd_date, crd.err_code, crd.avatar cameraAvatar, c.facesluice
            FROM tbl_student s
                     JOIN tbl_bus b ON s.bus_id = b.id
                     JOIN tbl_camera c ON b.id = c.bus_id
                     LEFT JOIN (SELECT crd2.*, cr2.created_at, cr2.camera_id
                                FROM (SELECT crd1.student_id, max(cr1.created_at) lastest
                                      FROM tbl_camera_request_detail crd1
                                               JOIN tbl_camera_request cr1 ON crd1.camera_request_id = cr1.id
                                      GROUP BY student_id) l
                                         JOIN tbl_camera_request cr2 ON l.lastest = cr2.created_at
                                         JOIN tbl_camera_request_detail crd2
                                              ON cr2.id = crd2.camera_request_id AND crd2.student_id = l.student_id) crd
                               ON s.id = crd.student_id AND c.facesluice = crd.camera_id
            WHERE crd.err_code is null OR crd.err_code != 0 OR s.avatar != crd.avatar
            ORDER BY c.facesluice
            """, nativeQuery = true)
    List<Object[]> findLatestUnsuccessfulInsertion();
}
