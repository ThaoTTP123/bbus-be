package com.fpt.bbusbe.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.bbusbe.exception.ImportException;
import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.student.*;
import com.fpt.bbusbe.model.dto.response.excel.StudentImportResult;
import com.fpt.bbusbe.model.dto.response.student.StudentCameraResponse;
import com.fpt.bbusbe.model.dto.response.student.StudentPageResponse;
import com.fpt.bbusbe.model.dto.response.student.StudentResponse;
import com.fpt.bbusbe.model.entity.CameraRequest;
import com.fpt.bbusbe.model.entity.CameraRequestDetail;
import com.fpt.bbusbe.model.entity.Parent;
import com.fpt.bbusbe.model.entity.Student;
import com.fpt.bbusbe.model.enums.CameraRequestStatus;
import com.fpt.bbusbe.model.enums.CameraRequestType;
import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.mqtt.OperationType;
import com.fpt.bbusbe.repository.*;
import com.fpt.bbusbe.service.MqttService;
import com.fpt.bbusbe.service.S3Service;
import com.fpt.bbusbe.service.StudentService;
import com.fpt.bbusbe.service.UserService;
import com.fpt.bbusbe.utils.ExcelHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fpt.bbusbe.model.dto.response.student.StudentCameraResponse.setStatus;

@Service
@Slf4j(topic = "STUDENT-SERVICE")
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private static final String INVALID_DATA_EXCEPTION_PARENT_NOT_FOUND = "Parent not found";
    private static final String LOG_FIND_ALL_STUDENTS_START = "Find all students start";
    private static final String SORT_REGEX_PATTERN = "(\\w+?)(:)(.*)";

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final UserService userService;
    private final CheckpointRepository checkpointRepository;
    private final S3Service s3Service;
    private final MqttService mqttService;
    private final CameraRequestRepository cameraRequestRepository;
    private final CameraRequestDetailRepository cameraRequestDetailRepository;

    @Override
    public long countTotalStudents() {
        return studentRepository.count();
    }

    @Override
    public StudentPageResponse findAll(String keyword, String sort, int page, int size) {
        log.info(LOG_FIND_ALL_STUDENTS_START);

        // Sorting
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile(SORT_REGEX_PATTERN); // tencot:asc|desc
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    order = new Sort.Order(Sort.Direction.ASC, columnName);
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, columnName);
                }
            }
        }

        // Xu ly truong hop FE muon bat dau voi page = 1
        int pageNo = 0;
        if (page > 0) {
            pageNo = page - 1;
        }

        // Paging
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        Page<Student> entityPage;

        if (StringUtils.hasLength(keyword)) {
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = studentRepository.searchByKeyword(keyword, pageable);
        } else {
            entityPage = studentRepository.findAllStudent(pageable);
        }

        return getStudentPageResponse(page, size, entityPage);
    }

    @Override
    public List<StudentCameraResponse> getStudentsByBusId(UUID busId) {
        List<Object[]> results = studentRepository.findByBus_Id(busId);
        return results.stream().map(student -> StudentCameraResponse.builder()
                .studentId((UUID) student[0])
                .rollNumber((String) student[1])
                .studentName((String) student[2])
                .gender(Gender.valueOf((String) student[3]))
                .address((String) student[4])
                .parentName((String) student[5])
                .checkpointName((String) student[6])
                .status(setStatus((int) student[7]))
                .build()
        ).toList();
    }


    @Override
    public StudentResponse findById(UUID id) {
        Student student = getStudentEntity(id);
        //Get presigned url image from s3
        String imageUrl = null;
        try {
            imageUrl = s3Service.generatePresignedUrl("students/" + student.getAvatar());
        } catch (Exception e) {
            log.error("Get student image failed: " + e.getMessage());
        }
        return StudentResponse.builder()
                .id(student.getId())
                .rollNumber(student.getRollNumber())
                .name(student.getName())
                .className(student.getClassName())
                .gender(student.getGender())
                .dob(student.getDob())
                .address(student.getAddress())
                .avatar(imageUrl)
                .status(student.getStatus())
                .busId(student.getBus() == null ? null : student.getBus().getId())
                .busName(student.getBus() == null ? "Chưa có xe" : student.getBus().getName())
                .parentId(student.getParent().getId())
                .parent(userService.findById(student.getParent().getUser().getId()))
                .checkpointId(student.getCheckpoint() == null ? null : student.getCheckpoint().getId())
                .checkpointName(student.getCheckpoint() == null ? "Chưa có điểm đón" : student.getCheckpoint().getName())
                .checkpointDescription(student.getCheckpoint() == null ? "Chưa có điểm đón" : student.getCheckpoint().getDescription())
                .createdAt(student.getCreatedAt() == null ? null : student.getCreatedAt())
                .updatedAt(student.getUpdatedAt() == null ? null : student.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Student save(StudentCreationRequest req) {
        Student studentByRollNumber = studentRepository.findByRollNumber(req.getRollNumber());
        if (studentByRollNumber != null) {
            throw new InvalidDataException("Student with this roll number: " + req.getRollNumber() + " already exists");
        }

        Parent parent = parentRepository
                .findById(req.getParentId()).orElseThrow(
                        () -> new InvalidDataException(INVALID_DATA_EXCEPTION_PARENT_NOT_FOUND)
                );

        MultipartFile avatar = req.getAvatar()[0];

        String rollNumber = "";

        // Tự động tăng mã học sinh
        String latestRollNumber = studentRepository.findMaxRollNumber(); // VD: "HS00123"
        int nextIndex = 1;

        if (latestRollNumber != null && latestRollNumber.startsWith("HS")) {
            try {
                nextIndex = Integer.parseInt(latestRollNumber.substring(2)) + 1;
            } catch (NumberFormatException e) {
                throw new InvalidDataException("Invalid roll number format in DB: " + latestRollNumber);
            }
        }

        rollNumber = String.format("HS%05d", nextIndex); // Ví dụ: HS000124

        Student student = Student.builder()
                .rollNumber(rollNumber)
                .name(req.getName())
                .className(req.getClassName())
                .gender(req.getGender())
                .dob(req.getDob())
                .address(req.getAddress())
                .avatar(avatar.getOriginalFilename())
                .status(req.getStatus())
                .parent(parent)
                .build();

        studentRepository.save(student);

        try {
            String fileName = avatar.getOriginalFilename();
            s3Service.uploadFile(
                    "students/" + fileName,
                    avatar.getInputStream(),
                    avatar.getSize(),
                    avatar.getContentType()
            );
        } catch (IOException e) {
            log.error("Upload student image failed while creating: " + e.getMessage());
        }

        return student;
    }

    @Override
    public List<Student> importStudentsFromFile(MultipartFile file) {
        StudentImportResult result = ExcelHelper.excelToStudents(file);

        // Nếu có lỗi, không insert, mà ném exception chứa lỗi
        if (!result.getErrorRows().isEmpty()) {
            throw new ImportException("Import thất bại với nhiều lỗi trong file", result.getErrorRows());
        }

        // Nếu không có lỗi, thì tiếp tục insert
        return result.getValidStudents().stream().map(this::save).collect(Collectors.toList());
    }

    private Student save(StudentCreateNoImageRequest req) {
        Student studentByRollNumber = studentRepository.findByRollNumber(req.getRollNumber());
        if (studentByRollNumber != null) {
            throw new InvalidDataException("Student with this roll number: " + req.getRollNumber() + " already exists");
        }

        Parent parent = parentRepository
                .findById(req.getParentId()).orElseThrow(
                        () -> new InvalidDataException(INVALID_DATA_EXCEPTION_PARENT_NOT_FOUND)
                );

        Student student = Student.builder()
                .rollNumber(req.getRollNumber())
                .name(req.getName())
                .className(req.getClassName())
                .gender(req.getGender())
                .dob(req.getDob())
                .address(req.getAddress())
                .avatar(req.getAvatar())
                .status(req.getStatus())
                .parent(parent)
                .build();

        studentRepository.save(student);

        return student;
    }

    @Override
    public void update(StudentUpdateRequest req) {
        Student student = studentRepository
                .findByRollNumber(req.getRollNumber());
        if (student != null)
            if (!student.getId().equals(req.getId()))
                throw new InvalidDataException("A student with this roll number already exists");

        log.info("Updating user {}", req);
        //get user by id
        student = studentRepository.findById(req.getId()).orElseThrow(() -> new InvalidDataException("Student not found"));

        //set data
        if (req.getRollNumber() != null) student.setRollNumber(req.getRollNumber());
        if (req.getName() != null) student.setName(req.getName());
        if (req.getGender() != null) student.setGender(req.getGender());
        if (req.getClassName() != null) student.setClassName(req.getClassName());
        if (req.getDob() != null) student.setDob(req.getDob());
        if (req.getAddress() != null) student.setAddress(req.getAddress());
        if (req.getParentId() != null)
            student.setParent(parentRepository.findById(req.getParentId()).orElseThrow(() -> new InvalidDataException("Parent not found")));
        if (req.getCheckpointId() != null)
            student.setCheckpoint(checkpointRepository.findById(req.getCheckpointId()).orElseThrow(() -> new InvalidDataException("Checkpoint not found")));

        //save
        studentRepository.save(student);
    }

    @Override
    @Transactional
    public String updateAvatar(StudentUpdateAvatarRequest studentUpdateAvatarRequest) throws JsonProcessingException {
        Student student = studentRepository
                .findById(studentUpdateAvatarRequest.getId()).orElseThrow(() -> new InvalidDataException("Student not found"));
        student.setAvatar(studentUpdateAvatarRequest.getAvatar().getOriginalFilename());
        studentRepository.save(student);

        MultipartFile file = studentUpdateAvatarRequest.getAvatar();
        String fileName = file.getOriginalFilename();
        try {
            s3Service.uploadFile(
                    "students/" + fileName,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );
        } catch (IOException e) {
            log.error("Upload student image failed while updating: " + e.getMessage());
        }

        //If the student belongs to a bus has camera, update the image in camera
        if (student.getBus() != null && student.getBus().getCamera() != null) {
            CameraRequest cameraRequest = cameraRequestRepository.save(CameraRequest.builder()
                    .camera(student.getBus().getCamera())
                    .status(CameraRequestStatus.FAILED)
                    .requestType(CameraRequestType.EDIT)
                    .build());
            cameraRequestDetailRepository.save(
                    CameraRequestDetail.builder()
                            .id(new CameraRequestDetail.CameraRequestDetailId(cameraRequest, student))
                            .avatar(fileName)
                            .errCode(1)
                            .build()
            );
            mqttService.publishStudentsList(List.of(student), OperationType.EDIT, student.getBus().getCamera().getFacesluice());
        }

        return s3Service.generatePresignedUrl("students/" + fileName);
    }

    @Override
    public void delete(UUID id) {
        //get user by id
        Student student = getStudentEntity(id);
        studentRepository.delete(student);
        //delete student image in s3
        try {
            s3Service.deleteFile("students/" + student.getAvatar());
        } catch (Exception e) {
            log.error("Delete failed: " + e.getMessage());
        }
        log.info("Deleted user {}", id);
    }

    @Override
    public void changeStatus(StudentUpdateStatusRequest req) {
        //get user by id
        Student student = getStudentEntity(req.getId());
        student.setStatus(req.getStatus());
        studentRepository.save(student);
        log.info("Change status of user {}", req.getId());
    }

    private Student getStudentEntity(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private StudentPageResponse getStudentPageResponse(int page, int size, Page<Student> studentEntities) {
        log.info("Convert Student Entity Page");

        List<StudentResponse> studentList = studentEntities
                .stream()
                .map(entity -> {
                            //Get presigned url image from s3
                            String imageUrl = null;
                            try {
                                imageUrl = s3Service.generatePresignedUrl("students/" + entity.getAvatar());
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            }
                            return StudentResponse.builder()
                                    .id(entity.getId())
                                    .rollNumber(entity.getRollNumber())
                                    .name(entity.getName())
                                    .className(entity.getClassName() == null ? "Chưa có lớp" : entity.getClassName())
                                    .gender(entity.getGender())
                                    .dob(entity.getDob())
                                    .address(entity.getAddress())
                                    .avatar(imageUrl)
                                    .busId(entity.getBus() == null ? null : entity.getBus().getId())
                                    .busName(entity.getBus() == null ? "Chưa có xe" : entity.getBus().getName())
                                    .status(entity.getStatus())
                                    .parentId(entity.getParent().getId())
                                    .parent(userService.findById(entity.getParent().getUser().getId()))
                                    .checkpointId(entity.getCheckpoint() == null ? null : entity.getCheckpoint().getId())
                                    .checkpointName(entity.getCheckpoint() == null ? "Chưa có điểm đón" : entity.getCheckpoint().getName())
                                    .checkpointDescription(entity.getCheckpoint() == null ? "Chưa có điểm đón" : entity.getCheckpoint().getDescription())
                                    .createdAt(entity.getCreatedAt() == null ? null : entity.getCreatedAt())
                                    .updatedAt(entity.getUpdatedAt() == null ? null : entity.getUpdatedAt())
                                    .build();
                        }
                ).toList();

        StudentPageResponse response = new StudentPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(studentEntities.getTotalElements());
        response.setTotalPages(studentEntities.getTotalPages());
        response.setStudents(studentList);

        return response;
    }
}
