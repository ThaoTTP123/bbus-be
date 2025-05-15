package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.CameraRequest;
import com.fpt.bbusbe.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraRequestRepository extends JpaRepository<CameraRequest, String> {

    Page<CameraRequest> findAllByCamera_Facesluice(String cameraFacesluice, Pageable pageable);

    Page<CameraRequest> findAllByStatus(boolean status, Pageable pageable);

    CameraRequest findTopByCamera_FacesluiceOrderByCreatedAtDesc(String cameraFacesluice);
}
