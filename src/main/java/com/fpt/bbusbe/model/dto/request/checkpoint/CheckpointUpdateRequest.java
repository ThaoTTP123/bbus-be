package com.fpt.bbusbe.model.dto.request.checkpoint;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckpointUpdateRequest {

    private UUID id;

    @NotBlank(message = "Checkpoint name cannot be blank")
    private String checkpointName;

    private String description;

    @NotBlank(message = "Checkpoint latitude cannot be blank")
    private String latitude;

    @NotBlank(message = "Checkpoint longitude cannot be blank")
    private String longitude;
}
