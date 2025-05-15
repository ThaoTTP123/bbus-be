package com.fpt.bbusbe.model.entity;

import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusDirection;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_attendance")
public class Attendance extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private LocalDate date;

    @Column(name = "direction")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BusDirection direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id")
    private Checkpoint checkpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private AttendanceStatus status;

    @Column(name = "checkin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date checkin;

    @Column(name = "checkout")
    @Temporal(TemporalType.TIMESTAMP)
    private Date checkout;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "assigned_to")
    private String assignedTo;
}
