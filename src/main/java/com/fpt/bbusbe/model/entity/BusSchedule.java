package com.fpt.bbusbe.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fpt.bbusbe.model.enums.BusDirection;
import com.fpt.bbusbe.model.enums.BusScheduleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Set;


@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_bus_schedule")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BusSchedule extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @Column(name = "date")
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "bus_schedule_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BusScheduleStatus busScheduleStatus;

    @Column(name = "note")
    private String note;

    @Column(name = "direction")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BusDirection direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_id")
    private Assistant assistant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;
}
