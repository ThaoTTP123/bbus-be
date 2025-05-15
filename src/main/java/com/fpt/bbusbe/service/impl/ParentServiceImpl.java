package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ForBiddenException;
import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.student.StudentUpdateByParentRequest;
import com.fpt.bbusbe.model.dto.response.bus.BusResponse;
import com.fpt.bbusbe.model.dto.response.parent.ParentPageResponse;
import com.fpt.bbusbe.model.dto.response.parent.ParentResponse;
import com.fpt.bbusbe.model.dto.response.student.StudentResponse;
import com.fpt.bbusbe.model.entity.*;
import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusStatus;

import com.fpt.bbusbe.repository.*;
import com.fpt.bbusbe.model.mqtt.OperationType;
import com.fpt.bbusbe.repository.BusRepository;
import com.fpt.bbusbe.repository.CheckpointRepository;
import com.fpt.bbusbe.repository.ParentRepository;
import com.fpt.bbusbe.repository.StudentRepository;
import com.fpt.bbusbe.service.MqttService;
import com.fpt.bbusbe.service.ParentService;
import com.fpt.bbusbe.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fpt.bbusbe.utils.TokenUtils.getUserLoggedInId;

@Service
@Slf4j(topic = "PARENT-SERVICE")
public class ParentServiceImpl implements ParentService {

    private static final String SORT_REGEX_PATTERN = "(\\w+?)(:)(.*)";

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final BusRepository busRepository;
    private final CheckpointRepository checkpointRepository;
    private final CameraRepository cameraRepository;
    private final MqttService mqttService;
    private final S3Service s3Service;

    public ParentServiceImpl(StudentRepository studentRepository, ParentRepository parentRepository, BusRepository busRepository, CheckpointRepository checkpointRepository, CameraRepository cameraRepository, MqttService mqttService, S3Service s3Service) {
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.busRepository = busRepository;
        this.checkpointRepository = checkpointRepository;
        this.cameraRepository = cameraRepository;
        this.mqttService = mqttService;
        this.s3Service = s3Service;
    }

    @Override
    public List<StudentResponse> findStudentsOfAParent() {
        List<Student> students = studentRepository.findByParent_User_Id(getUserLoggedInId());
        List<StudentResponse> studentResponses = new ArrayList<>();
        for (Student student : students) {

            String imageUrl = null;
            try {
                imageUrl = s3Service.generatePresignedUrl("students/" + student.getAvatar());
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            StudentResponse studentResponse = StudentResponse.builder()
                    .id(student.getId())
                    .name(student.getName())
                    .avatar(imageUrl)
                    .dob(student.getDob())
                    .address(student.getAddress())
                    .gender(student.getGender())
                    .status(student.getStatus())
                    .parentId(student.getParent().getId())
                    .rollNumber(student.getRollNumber())
                    .busId(student.getBus() != null ? student.getBus().getId() : null)
                    .busName(student.getBus() != null ? student.getBus().getName() : null)
                    .checkpointId(student.getCheckpoint() != null ? student.getCheckpoint().getId() : null)
                    .checkpointName(student.getCheckpoint() != null ? student.getCheckpoint().getName() : null)
                    .checkpointDescription(student.getCheckpoint() != null ? student.getCheckpoint().getDescription() : null)
                    .build();
            studentResponses.add(studentResponse);
        }
        return studentResponses;
    }

    @Override
    public ParentPageResponse findAll(String keyword, String sort, int page, int size) {
        // Implement the logic to find all parents with pagination and sorting
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

        Page<Parent> entityPage;

        if (StringUtils.hasLength(keyword)) {
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = parentRepository.searchByKeyword(keyword, pageable);
        } else {
            entityPage = parentRepository.findAll(pageable);
        }

        return getParentPageResponse(page, size, entityPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusResponse registerCheckpoint(UUID studentId, UUID checkpointId) {
        UUID parentUserId = getUserLoggedInId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

//        if (student.getParent().getUser().getId() != parentUserId) {
//            throw new ForBiddenException("You don't have permission to access this student.");
//        }

        if (student.getCheckpoint() != null) {
            throw new ForBiddenException("Student has already registered checkpoint.");
        }

        Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found."));

        if (checkpoint.getRoute() == null) {
            throw new InvalidDataException("Checkpoint has not been assigned to any route.");
        }

        Route route = checkpoint.getRoute();

        List<Bus> routeBuses = busRepository.findAllByRoute_Id(route.getId()).stream()
                .filter(bus -> bus.getAmountOfStudent() < bus.getMaxCapacity())
                .collect(Collectors.toList());

        Bus defaultBus = busRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .orElseThrow(() -> new ResourceNotFoundException("Default bus not found."));

        Optional<Bus> latestBusOpt = busRepository.findTopByNameOrderByNameDesc();
        int nextBusNumber = 1;
        if (latestBusOpt.isPresent()) {
            String[] parts = latestBusOpt.get().getName().split(" ");
            if (parts.length == 2) {
                try {
                    nextBusNumber = Integer.parseInt(parts[1]) + 1;
                } catch (NumberFormatException e) {
                    throw new InvalidDataException("Invalid bus name format: " + latestBusOpt.get().getName());
                }
            }
        }

        Iterator<Bus> busIterator = routeBuses.iterator();
        Bus currentBus = busIterator.hasNext() ? busIterator.next() : null;

        // Nếu không có bus trống hoặc đã đầy, tạo mới
        if (currentBus == null || currentBus.getAmountOfStudent() >= currentBus.getMaxCapacity()) {
            String formattedBusName = String.format("Bus %03d", nextBusNumber);
            String espId = String.format("%03d001", nextBusNumber);
            String facesluice = "1" + espId;

            currentBus = Bus.builder()
                    .name(formattedBusName)
                    .espId(espId)
                    .route(route)
                    .status(BusStatus.ACTIVE)
                    .maxCapacity(defaultBus.getMaxCapacity())
                    .amountOfStudent(0)
                    .build();

            currentBus = busRepository.save(currentBus);

            Camera camera = cameraRepository.findByFacesluice(facesluice);
            if (camera == null) {
                camera = new Camera();
                camera.setFacesluice(facesluice);
            }
            camera.setBus(currentBus);
            cameraRepository.save(camera);
        }

        currentBus.setAmountOfStudent(currentBus.getAmountOfStudent() + 1);
        busRepository.save(currentBus);

        student.setBus(currentBus);
        student.setCheckpoint(checkpoint);
        studentRepository.save(student);

        return BusServiceImpl.getBusResponse(currentBus);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BusResponse> registerCheckpointForAllChildren(UUID checkpointId) {
        UUID parentUserId = getUserLoggedInId();

        // Lấy danh sách tất cả học sinh của phụ huynh
        List<Student> allChildren = studentRepository.findByParent_User_Id(parentUserId);

        if (allChildren.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy học sinh nào thuộc phụ huynh.");
        }

        // Nếu có bất kỳ học sinh nào đã có checkpoint thì không cho đăng ký hàng loạt
        boolean anyHasCheckpoint = allChildren.stream()
                .anyMatch(student -> student.getCheckpoint() != null);
        if (anyHasCheckpoint) {
            throw new InvalidDataException("Một hoặc nhiều học sinh đã được đăng ký checkpoint. Vui lòng sử dụng chức năng đăng ký riêng lẻ.");
        }

        // Lấy danh sách học sinh chưa đăng ký checkpoint (về mặt logic lúc này là tất cả)
        List<Student> children = allChildren; // có thể bỏ lọc vì đã kiểm tra ở trên

        Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint không tồn tại."));

        if (checkpoint.getRoute() == null) {
            throw new InvalidDataException("Checkpoint chưa được gán cho route nào.");
        }

        Route route = checkpoint.getRoute();

        List<Bus> routeBuses = busRepository.findAllByRoute_Id(route.getId()).stream()
                .filter(bus -> bus.getAmountOfStudent() < bus.getMaxCapacity())
                .collect(Collectors.toList());

        Bus defaultBus = busRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bus mặc định."));

        List<BusResponse> responses = new ArrayList<>();

        Optional<Bus> latestBusOpt = busRepository.findTopByNameOrderByNameDesc();
        int nextBusNumber = 1;
        if (latestBusOpt.isPresent()) {
            String[] parts = latestBusOpt.get().getName().split(" ");
            if (parts.length == 2) {
                try {
                    nextBusNumber = Integer.parseInt(parts[1]) + 1;
                } catch (NumberFormatException e) {
                    throw new InvalidDataException("Bus name format is invalid: " + latestBusOpt.get().getName());
                }
            }
        }

        Iterator<Bus> busIterator = routeBuses.iterator();
        Bus currentBus = busIterator.hasNext() ? busIterator.next() : null;

        for (Student student : children) {
            if (currentBus == null || currentBus.getAmountOfStudent() >= currentBus.getMaxCapacity()) {
                String formattedBusName = String.format("Bus %03d", nextBusNumber);
                String espId = String.format("%03d001", nextBusNumber);
                String facesluice = "1" + espId;

                currentBus = Bus.builder()
                        .name(formattedBusName)
                        .espId(espId)
                        .route(route)
                        .status(BusStatus.ACTIVE)
                        .maxCapacity(defaultBus.getMaxCapacity())
                        .amountOfStudent(0)
                        .build();

                currentBus = busRepository.save(currentBus);
                nextBusNumber++;

                Camera camera = cameraRepository.findByFacesluice(facesluice);
                if (camera == null) {
                    camera = new Camera();
                    camera.setFacesluice(facesluice);
                }
                camera.setBus(currentBus);
                cameraRepository.save(camera);

                responses.add(BusServiceImpl.getBusResponse(currentBus));
            }

            currentBus.setAmountOfStudent(currentBus.getAmountOfStudent() + 1);
            busRepository.save(currentBus);

            student.setBus(currentBus);
            student.setCheckpoint(checkpoint);
            studentRepository.save(student);
        }

        return responses;
    }

    /**
     * Gỡ checkpoint và bus khỏi học sinh, đồng thời giảm số lượng học sinh trên bus.
     * Dùng để chuẩn bị cho việc đổi sang checkpoint mới.
     */
    private void removeStudentFromCurrentBusAndCheckpoint(Student student) {
        Bus currentBus = student.getBus();

        if (currentBus != null) {
            int newAmount = Math.max(0, currentBus.getAmountOfStudent() - 1);
            currentBus.setAmountOfStudent(newAmount);
            busRepository.save(currentBus);
        }

        student.setCheckpoint(null);
        student.setBus(null);
        studentRepository.save(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusResponse upsertCheckpoint(UUID studentId, UUID checkpointId) {
        UUID parentUserId = getUserLoggedInId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getParent().getUser().getId().equals(parentUserId)) {
            throw new ForBiddenException("You don't have permission to access this student.");
        }

        // Nếu học sinh đã có checkpoint → đổi
        if (student.getCheckpoint() != null) {
            removeStudentFromCurrentBusAndCheckpoint(student);
        }

        //Tìm tất cả các attendance trong tương lai của student này đổi thành checkpoint mới
        List<Attendance> attendanceList = student.getAttendances().stream()
                .filter(attendance -> attendance.getDate().isAfter(LocalDate.now()))
                .collect(Collectors.toList());
        for (Attendance attendance : attendanceList) {
            attendance.setCheckpoint(checkpointRepository.findById(checkpointId)
                    .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found")));
            attendance.setStatus(AttendanceStatus.ABSENT);
        }

        // Đăng ký lại (dùng lại logic cũ)
        return registerCheckpoint(studentId, checkpointId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BusResponse> upsertCheckpointForAll(UUID checkpointId) {
        UUID parentId = getUserLoggedInId();

        List<Student> children = studentRepository.findByParent_User_Id(parentId);

        if (children.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy học sinh nào thuộc phụ huynh.");
        }

        for (Student student : children) {
            if (student.getCheckpoint() != null) {
                removeStudentFromCurrentBusAndCheckpoint(student);
            }
        }

        //Tìm tất cả các attendance trong tương lai của tẩt cả các con đổi thành checkpoint mới
        List<Attendance> attendanceList = new ArrayList<>();
        for (Student student : children) {
            attendanceList.addAll(student.getAttendances().stream()
                    .filter(attendance -> attendance.getDate().isAfter(LocalDate.now()))
                    .collect(Collectors.toList()));
        }
        for (Attendance attendance : attendanceList) {
            attendance.setCheckpoint(checkpointRepository.findById(checkpointId)
                    .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found")));
            attendance.setStatus(AttendanceStatus.ABSENT);
        }

        // Gọi lại logic gán checkpoint cho tất cả học sinh
        return registerCheckpointForAllChildren(checkpointId);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudentInfo(StudentUpdateByParentRequest request) {
        UUID parentUserId = getUserLoggedInId();

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Kiểm tra student này phải thuộc về phụ huynh đang đăng nhập
        if (!student.getParent().getUser().getId().equals(parentUserId)) {
            throw new ForBiddenException("Bạn không có quyền cập nhật thông tin của học sinh này");
        }

        // Update nếu giá trị không null
        if (request.getName() != null && !request.getName().isEmpty()) {
            student.setName(request.getName());
        }

        if (request.getDob() != null) {
            student.setDob(request.getDob());
        }

        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            student.setAddress(request.getAddress());
        }

        studentRepository.save(student);
    }


    private ParentPageResponse getParentPageResponse(int page, int size, Page<Parent> parentEntities) {
        log.info("Convert Student Entity Page");

        List<ParentResponse> parentList = parentEntities.stream().map(entity -> ParentResponse.builder()
                .id(entity.getId())
                .name(entity.getUser().getName())
                .email(entity.getUser().getEmail())
                .phone(entity.getUser().getPhone())
                .dob(entity.getUser().getDob())
                .address(entity.getUser().getAddress())
                .status(entity.getUser().getStatus())
                .avatar(entity.getUser().getAvatar())
                .gender(entity.getUser().getGender())
                .build()
        ).toList();

        ParentPageResponse response = new ParentPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(parentEntities.getTotalElements());
        response.setTotalPages(parentEntities.getTotalPages());
        response.setParents(parentList);

        return response;
    }
}
