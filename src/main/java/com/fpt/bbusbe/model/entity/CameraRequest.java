package com.fpt.bbusbe.model.entity;

import com.fpt.bbusbe.model.enums.CameraRequestStatus;
import com.fpt.bbusbe.model.enums.CameraRequestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "tbl_camera_request")
public class CameraRequest extends AbstractEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id")
    private Camera camera;

    @Column(name = "request_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CameraRequestType requestType;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CameraRequestStatus status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id.cameraRequest", cascade = CascadeType.ALL)
    private List<CameraRequestDetail> cameraRequestDetails;

}
