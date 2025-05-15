package com.fpt.bbusbe.model.dto.response.requestType;

import com.fpt.bbusbe.model.dto.response.PageResponseAbstract;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class RequestTypePageResponse extends PageResponseAbstract implements Serializable {
    private List<RequestTypeResponse> requestTypes;
}
