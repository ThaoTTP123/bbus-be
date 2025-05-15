package com.fpt.bbusbe.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_route")
public class Route extends AbstractEntity {
    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "path")
    private String path;

    @Column(name = "description")
    private String description;

    @Column(name = "period_start")
    @Temporal(TemporalType.DATE)
    private Date periodStart;

    @Column(name = "period_end")
    @Temporal(TemporalType.DATE)
    private Date periodEnd;

    @Column(name = "checkpoint_time")
    private String checkpointTime;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "route")
    private Set<Checkpoint> checkpoints;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "route", cascade = CascadeType.ALL)
    private Set<Bus> buses;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "route", cascade = CascadeType.ALL)
    private Set<BusSchedule> busSchedules;
}