package com.fpt.bbusbe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.bbusbe.model.dto.request.student.StudentCreationRequest;
import com.fpt.bbusbe.model.dto.request.student.StudentUpdateAvatarRequest;
import com.fpt.bbusbe.model.dto.request.student.StudentUpdateRequest;
import com.fpt.bbusbe.model.dto.request.student.StudentUpdateStatusRequest;
import com.fpt.bbusbe.model.dto.response.student.StudentCameraResponse;
import com.fpt.bbusbe.model.dto.response.student.StudentPageResponse;
import com.fpt.bbusbe.model.dto.response.student.StudentResponse;
import com.fpt.bbusbe.model.entity.Student;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface StudentService {

    StudentPageResponse findAll(String keyword, String sort, int page, int size);

    StudentResponse findById(UUID id);

    Student save(StudentCreationRequest req);

    List<Student> importStudentsFromFile(MultipartFile file);

    void update(StudentUpdateRequest req);

    void delete(UUID id);

    void changeStatus(@Valid StudentUpdateStatusRequest req);

    String updateAvatar(@Valid StudentUpdateAvatarRequest studentUpdateAvatarRequest) throws JsonProcessingException;

    List<StudentCameraResponse> getStudentsByBusId(UUID busId);

    long countTotalStudents();

}
