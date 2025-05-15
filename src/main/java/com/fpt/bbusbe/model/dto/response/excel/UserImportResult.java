package com.fpt.bbusbe.model.dto.response.excel;

import com.fpt.bbusbe.model.dto.request.user.UserCreationNoImageRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Getter
@Setter
public class UserImportResult {
    private List<UserCreationNoImageRequest> validUsers;
    private Map<Integer, String> errorRows; // row index -> message
}
