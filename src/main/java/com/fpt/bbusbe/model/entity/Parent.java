package com.fpt.bbusbe.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "tbl_parent")
public class Parent extends AbstractEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(
            mappedBy = "parent",
            cascade = CascadeType.ALL
    )
    @JsonIgnore
    private Set<Student> students;

}