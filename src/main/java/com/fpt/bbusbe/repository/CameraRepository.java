package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Bus;
import com.fpt.bbusbe.model.entity.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CameraRepository extends JpaRepository<Camera, String> {

    Camera findByFacesluice(String facesluice);
}
