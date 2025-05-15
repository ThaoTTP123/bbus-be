package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.request.busSchedule.BusScheduleCreationRequest;
import com.fpt.bbusbe.model.dto.request.busSchedule.BusScheduleStatusChangeRequest;
import com.fpt.bbusbe.model.dto.request.busSchedule.BusScheduleUpdateRequest;
import com.fpt.bbusbe.model.dto.request.busSchedule.CompleteBusScheduleRequest;
import com.fpt.bbusbe.model.dto.response.busSchedule.*;
import com.fpt.bbusbe.model.dto.response.busSchedule.BusSchedulePageResponse;
import com.fpt.bbusbe.model.dto.response.busSchedule.BusScheduleResponse;
import com.fpt.bbusbe.model.entity.Attendance;
import com.fpt.bbusbe.model.entity.Bus;
import com.fpt.bbusbe.model.entity.BusSchedule;
import com.fpt.bbusbe.model.entity.Student;
import com.fpt.bbusbe.model.enums.AttendanceStatus;
import com.fpt.bbusbe.model.enums.BusDirection;
import com.fpt.bbusbe.model.enums.BusScheduleStatus;
import com.fpt.bbusbe.repository.AttendanceRepository;
import com.fpt.bbusbe.repository.BusRepository;
import com.fpt.bbusbe.repository.BusScheduleRepository;
import com.fpt.bbusbe.service.BusScheduleService;
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
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "BUS-SCHEDULE-SERVICE")
public class BusScheduleServiceImpl implements BusScheduleService {

    private static final String SORT_REGEX_PATTERN = "(\\w+)(\\s+(asc|desc))?";

    private final BusScheduleRepository busScheduleRepository;
    private final BusRepository busRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int assignSchedulesForAllBusesOnDates(List<LocalDate> dates) {
        List<Bus> buses = busRepository.findAll();
        int total = 0;

        for (LocalDate date : dates) {
            for (Bus bus : buses) {
                if (bus.getDriver() == null || bus.getAssistant() == null || bus.getRoute() == null) continue;

                for (boolean direction : List.of(true, false)) {
                    // Kiểm tra lịch đã tồn tại chưa (direction-based)
                    BusDirection currentDirection = direction ? BusDirection.PICK_UP : BusDirection.DROP_OFF;
                    boolean exists = busScheduleRepository.findByDateAndDriver_User_Id(date, bus.getDriver().getUser().getId())
                            .stream()
                            .anyMatch(bs -> bs.getBus().getId().equals(bus.getId())
                                    && bs.getDate().equals(date)
                                    && bs.getRoute().getId().equals(bus.getRoute().getId())
                                    && bs.getDirection() == currentDirection);



                    if (!exists) {
                        BusSchedule schedule = BusSchedule.builder()
                                .bus(bus)
                                .route(bus.getRoute())
                                .driver(bus.getDriver())
                                .assistant(bus.getAssistant())
                                .date(date)
                                .direction(direction ? BusDirection.PICK_UP : BusDirection.DROP_OFF)
                                .busScheduleStatus(BusScheduleStatus.PENDING)
                                .build();
                        busScheduleRepository.save(schedule);
                        total++;

                        // Tạo attendance mặc định ABSENT cho học sinh trong bus
                        List<Attendance> attendances = new ArrayList<>();
                        for (Student student : bus.getStudents()) {
                            Attendance attendance = Attendance.builder()
                                    .student(student)
                                    .date(date)
                                    .direction(direction ? BusDirection.PICK_UP : BusDirection.DROP_OFF)
                                    .bus(bus)
                                    .checkpoint(student.getCheckpoint())
                                    .status(AttendanceStatus.ABSENT)
                                    .build();
                            attendances.add(attendance);
                        }
                        attendanceRepository.saveAll(attendances);
                    }
                }
            }
        }

        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeBusSchedule(CompleteBusScheduleRequest req) {
        BusSchedule busSchedule = busScheduleRepository.findById(req.getBusScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch xe"));

        // ✅ Lấy attendance chuẩn xác theo đúng busSchedule hiện tại
        List<Attendance> attendances = attendanceRepository.findAllByBusSchedule(
                busSchedule.getBus().getId(),
                busSchedule.getDate(),
                busSchedule.getDirection()
//                busSchedule.getRoute().getId()
        );

        boolean hasStudentsInBus = attendances.stream()
                .anyMatch(a -> a.getStatus() == AttendanceStatus.IN_BUS);

        if (hasStudentsInBus) {
            throw new InvalidDataException("Không thể kết thúc chuyến, vẫn còn học sinh trên xe!");
        }

        busSchedule.setBusScheduleStatus(BusScheduleStatus.COMPLETED);
        busSchedule.setNote(req.getNote());

        busScheduleRepository.save(busSchedule);
    }

    /**
     * Xóa tất cả lịch trình và điểm danh theo ngày
     *
     * @param date Ngày cần xóa lịch trình và điểm danh
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllSchedulesByDate(LocalDate date) {
        attendanceRepository.deleteByDate(date);
        busScheduleRepository.deleteByDate(date);
    }


    /**
     * @param monthStr YYYY-MM
     * @param sort     asc/desc
     * @param page     page number (1-based)
     * @param size     page size
     * @return BusScheduleDatePageResponse
     */
    @Override
    public BusScheduleDatePageResponse findDatesByMonth(String monthStr, String sort, int page, int size) {
        YearMonth yearMonth = YearMonth.parse(monthStr);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<LocalDate> fullDates = busScheduleRepository.findDistinctDatesByMonth(start, end);

        // sort
        if ("desc".equalsIgnoreCase(sort)) {
            fullDates.sort(Comparator.reverseOrder());
        } else {
            fullDates.sort(Comparator.naturalOrder());
        }

        int total = fullDates.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int pageIndex = Math.max(0, page - 1);
        int from = pageIndex * size;
        int to = Math.min(from + size, total);
        List<LocalDate> pagedDates = from < total ? fullDates.subList(from, to) : List.of();

        BusScheduleDatePageResponse response = new BusScheduleDatePageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(total);
        response.setTotalPages(totalPages);
        response.setDates(pagedDates);

        return response;
    }


    /**
     * @param monthStr YYYY-MM
     * @param sort     asc/desc
     * @param page     page number (1-based)
     * @param size     page size
     * @return BusSchedulePageResponse
     */
    @Override
    public BusSchedulePageResponse findByMonth(String monthStr, String sort, int page, int size) {
        YearMonth yearMonth = YearMonth.parse(monthStr); // 2025-04
        LocalDate start = yearMonth.atDay(1);            // 2025-04-01
        LocalDate end = yearMonth.atEndOfMonth();        // 2025-04-30

        Sort.Order order = Sort.Order.asc("date");
        if (StringUtils.hasLength(sort)) {
            Matcher matcher = Pattern.compile(SORT_REGEX_PATTERN).matcher(sort);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                order = "desc".equalsIgnoreCase(matcher.group(3))
                        ? Sort.Order.desc(columnName)
                        : Sort.Order.asc(columnName);
            }
        }

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(order));
        Page<BusSchedule> entityPage = busScheduleRepository.findByDateBetween(start, end, pageable);

        return getBusSchedulePageResponse(page, size, entityPage);
    }


    @Override
    public BusSchedulePageResponse findAll(int page, int size) {

        // Xu ly truong hop FE muon bat dau voi page = 1
        int pageNo = 0;
        if (page > 0) {
            pageNo = page - 1;
        }

        // Paging
        Pageable pageable = PageRequest.of(pageNo, size);

        Page<BusSchedule> entityPage = busScheduleRepository.findAll(pageable);

        return getBusSchedulePageResponse(page, size, entityPage);
    }

    private BusSchedulePageResponse getBusSchedulePageResponse(int page, int size, Page<BusSchedule> busScheduleEntities) {
        log.info("Convert Student Entity Page");

        List<BusScheduleResponse> busScheduleList = busScheduleEntities.stream().map(entity ->
                BusScheduleResponse.builder()
                        .id(entity.getId())
                        .busId(entity.getId())
                        .name(entity.getBus().getName())
                        .licensePlate(entity.getBus().getLicensePlate())
                        .date(entity.getDate())
                        .driverId(entity.getDriver() != null ? entity.getDriver().getUser().getId() : null)
                        .driverName(entity.getDriver() != null ? entity.getDriver().getUser().getName() : null)
                        .assistantId(entity.getAssistant() != null ? entity.getAssistant().getUser().getId() : null)
                        .assistantName(entity.getAssistant() != null ? entity.getAssistant().getUser().getName() : null)
                        .route(entity.getRoute() != null ? entity.getRoute().getCode() : null)
                        .createdAt(entity.getCreatedAt())
                        .updatedAt(entity.getUpdatedAt())
                        .build()).toList();

        BusSchedulePageResponse response = new BusSchedulePageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(busScheduleEntities.getTotalElements());
        response.setTotalPages(busScheduleEntities.getTotalPages());
        response.setBusSchedules(busScheduleList);

        return response;
    }

    @Override
    public BusScheduleResponse findById(UUID id) {
        return null;
    }

    @Override
    public BusScheduleResponse save(BusScheduleCreationRequest req) {
        return null;
    }

    @Override
    public BusScheduleResponse update(BusScheduleUpdateRequest req) {
        return null;
    }

    @Override
    public void changeStatus(BusScheduleStatusChangeRequest req) {

    }

    @Override
    public void delete(UUID id) {

    }

    @Override
    public BusScheduleResponse findByEspIdForMqtt(String number) {
        return null;
    }

    @Override
    public void findByDate(Date date) {

    }
}