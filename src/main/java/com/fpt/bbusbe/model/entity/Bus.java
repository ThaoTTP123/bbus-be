package com.fpt.bbusbe.model.entity;

import com.fpt.bbusbe.model.enums.BusStatus;
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
@Table(name = "tbl_bus")
public class Bus extends AbstractEntity {
    @Column(name = "license_plate", unique = true)
    private String licensePlate;

    @Column(name = "name", unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_id")
    private Assistant assistant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "esp_id", length = 6, unique = true)
    private String espId;

    @Column(name = "amount_of_student")
    private Integer amountOfStudent;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BusStatus status;

    @OneToOne(mappedBy = "bus", cascade = CascadeType.ALL)
    private Camera camera;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bus", cascade = CascadeType.ALL)
    private Set<Attendance> attendances;

    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL)
    private Set<Student> students;

    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL)
    private Set<BusSchedule> busSchedules;
}
