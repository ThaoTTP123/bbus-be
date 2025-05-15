package com.fpt.bbusbe.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_request_type")
public class RequestType extends AbstractEntity {

    @Column(name = "request_type_name", nullable = false, unique = true)
    private String requestTypeName;

    @OneToMany(mappedBy = "requestType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Request> requests;


}
