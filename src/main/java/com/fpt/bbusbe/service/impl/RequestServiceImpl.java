package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.request.ReplyRequestRequest;
import com.fpt.bbusbe.model.dto.request.request.RequestCreationRequest;
import com.fpt.bbusbe.model.dto.response.bus.BusWithCheckpointResponse;
import com.fpt.bbusbe.model.dto.response.request.RequestPageResponse;
import com.fpt.bbusbe.model.dto.response.request.RequestResponse;
import com.fpt.bbusbe.model.entity.*;
import com.fpt.bbusbe.model.enums.RequestStatus;
import com.fpt.bbusbe.repository.*;
import com.fpt.bbusbe.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fpt.bbusbe.utils.TokenUtils.getUserLoggedInId;

@Service
@Slf4j(topic = "CHECKPOINT_SERVICE")
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final CheckpointRepository checkpointRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final BusRepository busRepository;

    @Override
    public long countPendingRequests() {
        return requestRepository.countByStatus(RequestStatus.PENDING);
    }

    @Override
    public long countTotalRequests() {
        return requestRepository.count();
    }

    @Override
    public RequestPageResponse findAll(String keyword, String sort, int page, int size) {
        log.info("findAll start");

        // Sorting
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // tencot:asc|desc
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

        // Xử lý trường hợp FE muốn bắt đầu với page = 1
        int pageNo = (page > 0) ? page - 1 : 0;

        // Paging
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        // Chuẩn hóa `keyword` (nếu có) để tránh lỗi query
        String formattedKeyword = (StringUtils.hasLength(keyword)) ? "%" + keyword.toLowerCase() + "%" : "%";

        Page<Request> entityPage = requestRepository.searchByKeyword(formattedKeyword, pageable);
        return getRequestPageResponse(page, size, entityPage);
    }

    @Override
    public List<RequestResponse> getMyRequests(UUID requestTypeId) {
        UUID userLoggedInId = getUserLoggedInId();

        List<Request> requests;

        if (requestTypeId != null) {
            requests = requestRepository.findAllBySendBy_IdAndRequestType_IdOrderByCreatedAtDesc(userLoggedInId, requestTypeId);
        } else {
            requests = requestRepository.findAllBySendBy_IdOrderByCreatedAtDesc(userLoggedInId);
        }

        return requests.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public RequestResponse findById(UUID id) {
        Request request = getRequestEntity(id);
        return RequestResponse.builder()
                .requestId(request.getId())
                .requestTypeId(request.getRequestType().getId())
                .requestTypeName(request.getRequestType().getRequestTypeName())
                .studentId(request.getStudent() != null ? request.getStudent().getId() : null)
                .studentName(request.getStudent() != null ? request.getStudent().getName() : null)
                .sendByUserId(request.getSendBy().getId())
                .sendByName(request.getSendBy().getName())
                .checkpointId(request.getCheckpoint() != null ? request.getCheckpoint().getId() : null)
                .checkpointName(request.getCheckpoint() != null ? request.getCheckpoint().getName() : null)
                .approvedByUserId(request.getApprovedBy() != null ? request.getApprovedBy().getId() : null)
                .approvedByName(request.getApprovedBy() != null ? request.getApprovedBy().getName() : null)
                .fromDate(request.getFromDate() != null ? request.getFromDate() : null)
                .toDate(request.getToDate() != null ? request.getToDate() : null)
                .reason(request.getReason())
                .reply(request.getReply())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public RequestResponse save(RequestCreationRequest req) {
        UUID userLoggedInId = getUserLoggedInId();
        User user = userRepository.getOne(userLoggedInId);

        if (req.getRequestTypeId() == null) {
            throw new ResourceNotFoundException("RequestTypeId là bắt buộc");
        }

        RequestType requestType = requestTypeRepository.getOne(req.getRequestTypeId());
        String requestTypeName = requestType.getRequestTypeName();
        LocalDate today = LocalDate.now();

        // ✅ Chặn gửi trùng theo ngày & nội dung đơn
        List<Request> existingRequests = requestRepository.findAllBySendBy_Id(userLoggedInId);

        long requestCountToday = existingRequests.stream()
                .filter(r ->
                        r.getRequestType().getId().equals(req.getRequestTypeId()) &&
                                r.getCreatedAt().toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                        .equals(today)
                )
                .count();

        if (requestCountToday >= 5) {
            throw new InvalidDataException("Bạn đã gửi tối đa 5 đơn loại này trong ngày hôm nay. Vui lòng chờ xét duyệt các đơn trước đó trước khi gửi thêm.");
        }

        // Kiểm tra đơn trùng hẳn (nội dung giống nhau)
        boolean isDuplicateContent = existingRequests.stream().anyMatch(r ->
                r.getRequestType().getId().equals(req.getRequestTypeId()) &&
                        Objects.equals(r.getStudent() != null ? r.getStudent().getId() : null, req.getStudentId()) &&
                        Objects.equals(r.getCheckpoint() != null ? r.getCheckpoint().getId() : null, req.getCheckpointId()) &&
                        Objects.equals(r.getReason(), req.getReason()) &&
                        Objects.equals(r.getFromDate(), req.getFromDate()) &&
                        Objects.equals(r.getToDate(), req.getToDate()) &&
                        r.getStatus() == RequestStatus.PENDING
        );

        if (isDuplicateContent) {
            throw new InvalidDataException("Bạn đã gửi 1 đơn giống nội dung này và đơn này đang trong quá trình xử lý. Vui lòng không gửi lại.");
        }

        Request request;

        // ✅ Đơn đổi checkpoint
        if (requestTypeName.equalsIgnoreCase("Yêu cầu đổi điểm đón/trả cho học sinh")) {
            if (req.getCheckpointId() == null) {
                throw new ResourceNotFoundException("CheckpointId là bắt buộc");
            }

            if (req.getStudentId() == null || req.getStudentId().toString().isEmpty()) {
                request = Request.builder()
                        .sendBy(user)
                        .requestType(requestType)
                        .checkpoint(checkpointRepository.getOne(req.getCheckpointId()))
                        .reason(req.getReason())
                        .status(RequestStatus.PENDING)
                        .build();
            } else {
                Parent parent = parentRepository.findByUserId(userLoggedInId);
//                Student student = studentRepository.findById(req.getStudentId())
//                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh"));
                Student student = studentRepository.getOne(req.getStudentId());
                if (!student.getParent().getId().equals(parent.getId())) {
                    throw new InvalidDataException("Học sinh không thuộc quyền quản lý của phụ huynh này");
                }

                request = Request.builder()
                        .sendBy(user)
                        .student(student)
                        .requestType(requestType)
                        .checkpoint(checkpointRepository.getOne(req.getCheckpointId()))
                        .reason(req.getReason())
                        .status(RequestStatus.PENDING)
                        .build();
            }


        }

        // ✅ Đơn xin nghỉ học
        else if (requestTypeName.equalsIgnoreCase("Đơn xin nghỉ học")) {
            Parent parent = parentRepository.findByUserId(userLoggedInId);
            Student student = studentRepository.findById(req.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh"));
            if (!student.getParent().getId().equals(parent.getId())) {
                throw new InvalidDataException("Học sinh không thuộc quyền quản lý của phụ huynh này");
            }

            request = Request.builder()
                    .sendBy(user)
                    .student(student)
                    .requestType(requestType)
                    .checkpoint(null)
                    .reason(req.getReason())
                    .fromDate(req.getFromDate())
                    .toDate(req.getToDate())
                    .status(RequestStatus.PENDING)
                    .build();
        }

        // ✅ Đơn khác
        else if (requestTypeName.equalsIgnoreCase("Đơn khác")) {
            Parent parent = parentRepository.findByUserId(userLoggedInId);
//            Student student = studentRepository.findById(req.getStudentId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh"));
//            if (!student.getParent().getId().equals(parent.getId())) {
//                throw new InvalidDataException("Học sinh không thuộc quyền quản lý của phụ huynh này");
//            }

            request = Request.builder()
                    .sendBy(user)
                    .requestType(requestType)
                    .checkpoint(null)
                    .reason(req.getReason())
                    .status(RequestStatus.PENDING)
                    .build();
        }

        else {
            throw new InvalidDataException("Loại đơn không hợp lệ");
        }

        requestRepository.save(request);
        return findById(request.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusWithCheckpointResponse processChangeCheckpointRequest(UUID requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn yêu cầu"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidDataException("Đơn này đã được xử lý hoặc không hợp lệ");
        }

        if (!request.getRequestType().getRequestTypeName().equalsIgnoreCase("Yêu cầu đổi điểm đón/trả cho học sinh")) {
            throw new InvalidDataException("Loại đơn không hợp lệ");
        }

        User parentUser = request.getSendBy();
        Parent parent = (Parent) parentRepository.findByUser(parentUser);
        if (parent == null) {
            throw new ResourceNotFoundException("Không tìm thấy phụ huynh gửi đơn");
        }

        Checkpoint newCheckpoint = request.getCheckpoint();
        if (newCheckpoint == null) {
            throw new InvalidDataException("Checkpoint mới không hợp lệ");
        }

        Route targetRoute = newCheckpoint.getRoute();
        List<Bus> busesInRoute = busRepository.findAllByRoute_Id(targetRoute.getId());

        List<Student> targetStudents;

        // ✅ Nếu có studentId trong request, xử lý cho học sinh đó
        if (request.getStudent() != null) {
            Student student = studentRepository.findById(request.getStudent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh"));

            if (!student.getParent().getId().equals(parent.getId())) {
                throw new InvalidDataException("Học sinh không thuộc quyền quản lý của phụ huynh này");
            }

            targetStudents = Collections.singletonList(student);
        }
        // ✅ Nếu không có studentId, xử lý cho tất cả học sinh con của phụ huynh
        else {
            targetStudents = studentRepository.findByParent_Id(parent.getId());
            if (targetStudents.isEmpty()) {
                throw new ResourceNotFoundException("Phụ huynh không có học sinh nào");
            }
        }

        int requiredSlots = (int) targetStudents.stream()
                .filter(s -> s.getBus() == null || !s.getBus().getRoute().getId().equals(targetRoute.getId()))
                .count();

        Bus targetBus = null;
        if (requiredSlots > 0) {
            targetBus = busesInRoute.stream()
                    .filter(bus -> bus.getMaxCapacity() - bus.getAmountOfStudent() >= requiredSlots)
                    .findFirst()
                    .orElse(null);

            if (targetBus == null) {
                request.setStatus(RequestStatus.REJECTED);
                request.setReply("Không có xe nào thuộc tuyến mới đủ chỗ trống");
                requestRepository.save(request);
                return null;
            }
        }

        for (Student student : targetStudents) {
            Bus oldBus = student.getBus();
            boolean needsBusChange = oldBus == null || !oldBus.getRoute().getId().equals(targetRoute.getId());

            if (needsBusChange && oldBus != null && targetBus != null && !oldBus.getId().equals(targetBus.getId())) {
                oldBus.setAmountOfStudent(oldBus.getAmountOfStudent() - 1);
                busRepository.save(oldBus);
            }

            student.setCheckpoint(newCheckpoint);
            student.setBus(needsBusChange ? targetBus : oldBus);
            studentRepository.save(student);

            List<Attendance> futureAttendances = attendanceRepository
                    .findAllByStudent_IdAndDateAfter(student.getId(), LocalDate.now());
            for (Attendance att : futureAttendances) {
                att.setCheckpoint(newCheckpoint);
                if (needsBusChange) att.setBus(targetBus);
            }
            attendanceRepository.saveAll(futureAttendances);
        }

        if (targetBus != null) {
            targetBus.setAmountOfStudent(targetBus.getAmountOfStudent() + requiredSlots);
            busRepository.save(targetBus);
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(userRepository.getOne(getUserLoggedInId()));
        request.setReply("Đã cập nhật checkpoint và xe mới cho học sinh.");
        requestRepository.save(request);

        // Trả về response cho 1 học sinh (ưu tiên học sinh đầu tiên trong danh sách)
        Student anyStudent = targetStudents.get(0);
        Bus finalBus = anyStudent.getBus();

        return BusWithCheckpointResponse.builder()
                .busId(finalBus.getId())
                .busName(finalBus.getName())
                .licensePlate(finalBus.getLicensePlate())
                .maxCapacity(finalBus.getMaxCapacity())
                .amountOfStudent(finalBus.getAmountOfStudent())
                .checkpointId(newCheckpoint.getId())
                .checkpointName(newCheckpoint.getName())
                .checkpointDescription(newCheckpoint.getDescription())
                .createdAt(request.getCreatedAt().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate())
                .updatedAt(request.getUpdatedAt().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate())
                .build();
    }



//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public BusWithCheckpointResponse processChangeCheckpointRequest(UUID requestId) {
//        Request request = requestRepository.findById(requestId)
//                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn yêu cầu"));
//
//        if (request.getStatus() != RequestStatus.PENDING) {
//            throw new InvalidDataException("Đơn này đã được xử lý hoặc không hợp lệ");
//        }
//
//        if (!request.getRequestType().getRequestTypeName().equalsIgnoreCase("Yêu cầu đổi điểm đón/trả cho học sinh")) {
//            throw new InvalidDataException("Loại đơn không hợp lệ");
//        }
//
//        User parentUser = request.getSendBy();
//        Parent parent = (Parent) parentRepository.findByUser(parentUser);
//        if (parent == null) {
//            throw new ResourceNotFoundException("Không tìm thấy phụ huynh gửi đơn");
//        }
//
//        List<Student> children = studentRepository.findByParent_Id(parent.getId());
//        if (children.isEmpty()) {
//            throw new ResourceNotFoundException("Phụ huynh không có học sinh nào");
//        }
//
//        Checkpoint newCheckpoint = request.getCheckpoint();
//        if (newCheckpoint == null) {
//            throw new InvalidDataException("Checkpoint mới không hợp lệ");
//        }
//
//        Route targetRoute = newCheckpoint.getRoute();
//        List<Bus> busesInRoute = busRepository.findAllByRoute_Id(targetRoute.getId());
//
//        int requiredSlots = (int) children.stream()
//                .filter(student -> student.getBus() == null || !student.getBus().getRoute().getId().equals(targetRoute.getId()))
//                .count();
//
//        // Nếu có học sinh cần chuyển bus → cần tìm bus có đủ chỗ
//        Bus targetBus = null;
//        if (requiredSlots > 0) {
//            targetBus = busesInRoute.stream()
//                    .filter(bus -> bus.getMaxCapacity() - bus.getAmountOfStudent() >= requiredSlots)
//                    .findFirst()
//                    .orElse(null);
//
//            if (targetBus == null) {
//                request.setStatus(RequestStatus.REJECTED);
//                request.setReply("Không có xe nào thuộc tuyến mới đủ chỗ trống");
//                requestRepository.save(request);
//                return null;
//            }
//        }
//
//        for (Student student : children) {
//            Bus oldBus = student.getBus();
//
//            // Nếu bus hiện tại đã thuộc route của checkpoint mới → không đổi bus
//            if (oldBus != null && oldBus.getRoute().getId().equals(targetRoute.getId())) {
//                student.setCheckpoint(newCheckpoint);
//                studentRepository.save(student);
//
//                // Cập nhật attendance tương lai
//                List<Attendance> attendances = attendanceRepository
//                        .findAllByStudent_IdAndDateAfter(student.getId(), LocalDate.now());
//                for (Attendance att : attendances) {
//                    att.setCheckpoint(newCheckpoint);
//                    // giữ nguyên bus
//                }
//                attendanceRepository.saveAll(attendances);
//                continue; // bỏ qua đổi bus
//            }
//
//            // Nếu cần đổi sang bus mới
//            if (oldBus != null && targetBus != null && !oldBus.getId().equals(targetBus.getId())) {
//                oldBus.setAmountOfStudent(oldBus.getAmountOfStudent() - 1);
//                busRepository.save(oldBus);
//            }
//
//            student.setCheckpoint(newCheckpoint);
//            student.setBus(targetBus);
//            studentRepository.save(student);
//
//            List<Attendance> futureAttendances = attendanceRepository
//                    .findAllByStudent_IdAndDateAfter(student.getId(), LocalDate.now());
//            for (Attendance att : futureAttendances) {
//                att.setCheckpoint(newCheckpoint);
//                att.setBus(targetBus);
//            }
//            attendanceRepository.saveAll(futureAttendances);
//        }
//
//        // Nếu có đổi bus → tăng số lượng học sinh
//        if (targetBus != null) {
//            targetBus.setAmountOfStudent(targetBus.getAmountOfStudent() + requiredSlots);
//            busRepository.save(targetBus);
//        }
//
//        // Cập nhật trạng thái đơn
//        User approver = userRepository.getOne(getUserLoggedInId());
//        request.setStatus(RequestStatus.APPROVED);
//        request.setApprovedBy(approver);
//        request.setReply("Đã cập nhật checkpoint và xe mới (nếu cần) cho học sinh.");
//        requestRepository.save(request);
//
//        return BusWithCheckpointResponse.builder()
//                .busId(targetBus != null ? targetBus.getId() : children.get(0).getBus().getId())
//                .busName(targetBus != null ? targetBus.getName() : children.get(0).getBus().getName())
//                .licensePlate(targetBus != null ? targetBus.getLicensePlate() : children.get(0).getBus().getLicensePlate())
//                .maxCapacity(targetBus != null ? targetBus.getMaxCapacity() : children.get(0).getBus().getMaxCapacity())
//                .amountOfStudent(targetBus != null ? targetBus.getAmountOfStudent() : children.get(0).getBus().getAmountOfStudent())
//                .checkpointId(newCheckpoint.getId())
//                .checkpointName(newCheckpoint.getName())
//                .checkpointDescription(newCheckpoint.getDescription())
//                .createdAt(request.getCreatedAt().toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDate())
//                .updatedAt(request.getUpdatedAt().toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDate())
//                .build();
//    }




    /**
     * Reply request (by teacher/business admin)
     *
     * @param req
     * @return
     */
    @SuppressWarnings("deprecation")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RequestResponse replyRequest(ReplyRequestRequest req) {
        log.info("replyRequest start");

        UUID userLoggedInId = getUserLoggedInId();

        // get request by id
        Request request = getRequestEntity(req.getRequestId());

        // update
        request.setApprovedBy(userRepository.getOne(userLoggedInId));
        request.setReply(req.getReply());
        request.setStatus(req.getStatus());

        Request r = requestRepository.save(request);
        return findById(r.getId());
    }

//    @Override
//    public void update(RequestUpdateRequest req) {
//
//    }

    @Override
    public void delete(UUID id) {

    }

    private Request getRequestEntity(UUID id) {
        return requestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    }


    private RequestResponse convertToResponse(Request request) {
        return RequestResponse.builder()
                .requestId(request.getId())
                .requestTypeId(request.getRequestType().getId())
                .requestTypeName(request.getRequestType().getRequestTypeName())
                .studentId(request.getStudent() != null ? request.getStudent().getId() : null)
                .studentName(request.getStudent() != null ? request.getStudent().getName() : null)
                .sendByUserId(request.getSendBy().getId())
                .sendByName(request.getSendBy().getName())
                .checkpointId(request.getCheckpoint() != null ? request.getCheckpoint().getId() : null)
                .checkpointName(request.getCheckpoint() != null ? request.getCheckpoint().getName() : null)
                .approvedByUserId(request.getApprovedBy() != null ? request.getApprovedBy().getId() : null)
                .approvedByName(request.getApprovedBy() != null ? request.getApprovedBy().getName() : null)
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .reason(request.getReason())
                .reply(request.getReply())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    /**
     * Convert RequestEntities to RequestResponse
     *
     * @param page
     * @param size
     * @param requestEntities
     * @return
     */
    private static RequestPageResponse getRequestPageResponse(int page, int size, Page<Request> requestEntities) {
        log.info("Convert Request Entity Page");

        List<RequestResponse> requestList = requestEntities.stream().map(entity -> RequestResponse.builder()
                .requestId(entity.getId())
                .requestTypeId(entity.getRequestType().getId())
                .requestTypeName(entity.getRequestType().getRequestTypeName())
                .studentId(entity.getStudent() != null ? entity.getStudent().getId() : null)
                .studentName(entity.getStudent() != null ? entity.getStudent().getName() : null)
                .sendByUserId(entity.getSendBy().getId())
                .sendByName(entity.getSendBy().getName())
                .checkpointId(entity.getCheckpoint() != null ? entity.getCheckpoint().getId() : null)
                .checkpointName(entity.getCheckpoint() != null ? entity.getCheckpoint().getName() : null)
                .approvedByUserId(entity.getApprovedBy() != null ? entity.getApprovedBy().getId() : null)
                .approvedByName(entity.getApprovedBy() != null ? entity.getApprovedBy().getName() : null)
                .fromDate(entity.getFromDate())
                .toDate(entity.getToDate())
                .reason(entity.getReason())
                .reply(entity.getReply())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build()
        ).toList();

        RequestPageResponse response = new RequestPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(requestEntities.getTotalElements());
        response.setTotalPages(requestEntities.getTotalPages());
        response.setRequests(requestList);

        return response;
    }
}
