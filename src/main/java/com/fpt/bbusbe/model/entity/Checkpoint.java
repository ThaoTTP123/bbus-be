package com.fpt.bbusbe.model.entity;

import com.fpt.bbusbe.model.enums.CheckpointStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_checkpoint")
public class Checkpoint extends AbstractEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "latitude")
    private String latitude;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private CheckpointStatus status;

    @Column(name = "description")
    private String description;

    @OneToMany(
            mappedBy = "checkpoint",
            cascade = CascadeType.ALL
    )
    private Set<Student> students;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "checkpoint", cascade = CascadeType.ALL)
    private Set<Attendance> attendances;

    @OneToMany(mappedBy = "checkpoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Request> requests;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;
}
