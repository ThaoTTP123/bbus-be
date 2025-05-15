package com.fpt.bbusbe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.bbusbe.model.dto.request.student.StudentCreationRequest;
import com.fpt.bbusbe.model.dto.request.student.StudentUpdateAvatarRequest;
import com.fpt.bbusbe.model.dto.request.student.StudentUpdateRequest;
import com.fpt.bbusbe.model.dto.request.student.StudentUpdateStatusRequest;
import com.fpt.bbusbe.model.dto.response.student.StudentPageResponse;
import com.fpt.bbusbe.model.dto.response.student.StudentResponse;
import com.fpt.bbusbe.model.entity.Student;
import com.fpt.bbusbe.service.StudentService;
import com.fpt.bbusbe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/student")
@Tag(name = "Student Controller")
@Slf4j(topic = "STUDENT-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class StudentController {

    private final StudentService studentService;
    private final UserService userService;

    @Operation(summary = "Get student list", description = "API retrieve students from db")
    @GetMapping("/list")
//    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        log.info("Get student list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "student list");
        result.put("data", studentService.findAll(keyword, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/by-bus")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    @Operation(summary = "Get students by bus ID", description = "API retrieve students by bus ID")
    public ResponseEntity<Object> getStudentsByBusId(@RequestParam UUID busId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get students by bus");
        result.put("data", studentService.getStudentsByBusId(busId));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @Operation(summary = "Get student detail", description = "API retrieve student detail by ID")
    @GetMapping("/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getStudentDetail(@PathVariable("studentId") UUID id
    ) {
        log.info("Get student detail by ID: {}", id);

        // Get logged in student
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        Student studentDetails = (Student) auth.getPrincipal();
//        UUID loggedInStudent = studentDetails.getId();

        //Check id
//        if (!loggedInStudent.equals(id)) {
//            throw new ForBiddenException("You are not allowed to access this resource");
//        }

        StudentResponse u = studentService.findById(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get student detail");
        result.put("data", u);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Create student", description = "API add new student to db")
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> createStudent(@ModelAttribute @Valid StudentCreationRequest studentCreationRequest
    ) {
        log.info("Create student: {}", studentCreationRequest);

        Student student = studentService.save(studentCreationRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "student created successfully");
        result.put("data", student.getRollNumber());

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Import student by excel", description = "API import students to db")
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> importStudents(@RequestParam("file") MultipartFile file
    ) {
        log.info("Import students from file");

        List<Student> students = studentService.importStudentsFromFile(file);

        // Convert the list of students to StudentResponse objects
        List<StudentResponse> studentResponses = students.stream().map(student -> StudentResponse.builder()
                .id(student.getId())
                .rollNumber(student.getRollNumber())
                .name(student.getName())
                .gender(student.getGender())
                .dob(student.getDob())
                .address(student.getAddress())
                .avatar(student.getAvatar())
                .status(student.getStatus())
                .parentId(student.getParent().getId())
                .parent(userService.findById(student.getParent().getUser().getId()))
                .build()
        ).toList();

        // Create the StudentPageResponse
        StudentPageResponse response = new StudentPageResponse();
        response.setPageNumber(0);
        response.setPageSize(studentResponses.size());
        response.setTotalElements(studentResponses.size());
        response.setTotalPages(1);
        response.setStudents(studentResponses);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "student created successfully");
        result.put("data", response);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update student", description = "API update student in db")
    @PutMapping("/upd")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> updateStudent(@RequestBody @Valid StudentUpdateRequest studentUpdateRequest
    ) {
        log.info("Updating student: {}", studentUpdateRequest);

        studentService.update(studentUpdateRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "student updated successfully");
        result.put("data", "");

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Update status", description = "API change status account for student")
    @PatchMapping("/change-status")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> changeStatus(@RequestBody @Valid StudentUpdateStatusRequest studentUpdateStatusRequest
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "student change status successfully");
        result.put("data", "");
        studentService.changeStatus(studentUpdateStatusRequest);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Update avatar", description = "API update avatar for student")
    @PatchMapping(value = "/update-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> updateAvatar(@ModelAttribute @Valid StudentUpdateAvatarRequest studentUpdateAvatarRequest
    ) throws JsonProcessingException {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "student change status successfully");
        result.put("data", studentService.updateAvatar(studentUpdateAvatarRequest));

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Delete student", description = "API inactivate student")
    @DeleteMapping("/del/{studentId}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> deleteStudent(@PathVariable("studentId") UUID id
    ) {
        log.info("Deleting student: {}", id);

        studentService.delete(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "student deleted successfully");
        result.put("data", "");

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

}