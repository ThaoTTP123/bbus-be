package com.fpt.bbusbe.model.dto.response.assistant;

import com.fpt.bbusbe.model.dto.response.PageResponseAbstract;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AssistantPageResponse extends PageResponseAbstract implements Serializable {
    private List<AssistantResponse> assistants;
}
