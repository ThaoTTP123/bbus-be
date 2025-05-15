package com.fpt.bbusbe.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.Set;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_student")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Student extends AbstractEntity {

    @Column(name = "roll_number", unique = true, nullable = false)
    private String rollNumber;

    @Column(name = "name")
    private String name;

    @Column(name = "class_name")
    private String className;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "dob")
    private Date dob;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(length = 255)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private StudentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id")
    private Checkpoint checkpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "student", cascade = CascadeType.ALL)
    private Set<Attendance> attendances;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Request> requests;


}
