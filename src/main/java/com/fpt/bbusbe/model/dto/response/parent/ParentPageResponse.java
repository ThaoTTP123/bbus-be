package com.fpt.bbusbe.model.dto.response.parent;

import com.fpt.bbusbe.model.dto.response.PageResponseAbstract;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ParentPageResponse extends PageResponseAbstract implements Serializable {
    private List<ParentResponse> parents;
}
