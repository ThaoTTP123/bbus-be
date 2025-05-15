package com.fpt.bbusbe.model.dto.response.checkpoint;

import com.fpt.bbusbe.model.dto.response.PageResponseAbstract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckpointWithRegisteredPageResponse extends PageResponseAbstract implements Serializable {
    private List<CheckpointWithRegisteredResponse> checkpoints;
}
