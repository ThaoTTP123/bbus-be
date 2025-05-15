package com.fpt.bbusbe.model.dto.response.cameraRequest;

import com.fpt.bbusbe.model.dto.response.PageResponseAbstract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class CameraRequestPageResponse extends PageResponseAbstract implements Serializable {
    private List<CameraRequestResponse> cameras;
}
