package com.fpt.bbusbe.model.dto.response.checkpoint;

import com.fpt.bbusbe.model.enums.BusStatus;
import com.fpt.bbusbe.model.enums.CheckpointStatus;
import com.fpt.bbusbe.model.enums.StudentStatus;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckpointWithStudentAndBus {
    private UUID checkpointId;
    private String checkpointName;
    private List<Student> students;
    private List<Bus> buses;
    private CheckpointStatus status;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Student {
        private UUID studentId;
        private String studentName;
        private String rollNumber;
        private UUID bus;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Bus {
        private UUID busId;
        private String busName;
        private BusStatus status;
        private String licensePlate;
    }
}
