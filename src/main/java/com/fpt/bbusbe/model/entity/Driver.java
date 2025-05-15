package com.fpt.bbusbe.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_driver")
public class Driver extends AbstractEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private Set<BusSchedule> busSchedules;
}
