package com.fpt.bbusbe.model.dto.response.excel;

import com.fpt.bbusbe.model.dto.request.student.StudentCreateNoImageRequest;
import com.fpt.bbusbe.model.dto.request.student.StudentCreationRequest;
import com.fpt.bbusbe.model.dto.request.user.UserCreationRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class StudentImportResult {
    private List<StudentCreateNoImageRequest> validStudents;
    private Map<Integer, String> errorRows; // row index -> message
}
