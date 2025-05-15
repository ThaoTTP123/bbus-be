package com.fpt.bbusbe.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "tbl_camera_request_detail")
public class CameraRequestDetail{

    @EmbeddedId
    private CameraRequestDetailId id;

    @Column(name = "roll_number")
    private String rollNumber;

    @Column(name = "name")
    private String name;

    @Column(name = "person_type")
    private int personType;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "err_code")
    private int errCode;

    @Embeddable
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CameraRequestDetailId {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "camera_request_id")
        private CameraRequest cameraRequest;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "student_id")
        private Student student;

    }
}
