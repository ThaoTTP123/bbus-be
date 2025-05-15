package com.fpt.bbusbe.model.dto.request.checkpoint;


import com.fpt.bbusbe.model.enums.CheckpointStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckpointStatusChangeRequest {
    private UUID id;
    private CheckpointStatus status;
}
