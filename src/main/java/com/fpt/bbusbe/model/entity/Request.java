package com.fpt.bbusbe.model.entity;

import com.fpt.bbusbe.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_request")
public class Request extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "send_by")
    private User sendBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_type_id")
    private RequestType requestType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id")
    private Checkpoint checkpoint;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "from_date")
    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @Column(name = "to_date")
    @Temporal(TemporalType.DATE)
    private Date toDate;

    @Column(name = "reply", columnDefinition = "TEXT")
    private String reply;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private RequestStatus status;

}
