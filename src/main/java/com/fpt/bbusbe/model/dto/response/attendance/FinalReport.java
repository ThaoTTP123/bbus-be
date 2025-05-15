package com.fpt.bbusbe.model.dto.response.attendance;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinalReport {
    private int grade;
    private long amountOfStudentRegistered;
    private long amountOfStudentDeregistered;
}
