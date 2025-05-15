package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.model.dto.response.busSchedule.BusScheduleResponse;
import com.fpt.bbusbe.model.dto.response.driver.DriverPageResponse;
import com.fpt.bbusbe.model.dto.response.driver.DriverResponse;
import com.fpt.bbusbe.model.entity.BusSchedule;
import com.fpt.bbusbe.model.entity.Driver;
import com.fpt.bbusbe.repository.BusScheduleRepository;
import com.fpt.bbusbe.repository.DriverRepository;
import com.fpt.bbusbe.service.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fpt.bbusbe.utils.TokenUtils.getUserLoggedInId;

@Service
@Slf4j(topic = "DRIVER-SERVICE")
public class DriverServiceImpl implements DriverService {
    
    private static final String SORT_REGEX_PATTERN = "(\\w+?)(:)(.*)";
    
    private final BusScheduleRepository busScheduleRepository;
    private final DriverRepository driverRepository;
    

    public DriverServiceImpl(BusScheduleRepository busScheduleRepository, DriverRepository driverRepository) {
        this.busScheduleRepository = busScheduleRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public DriverPageResponse findAll(String keyword, String sort, int page, int size) {
        // Implement the logic to find all drivers with pagination and sorting
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

        Page<Driver> entityPage;

        if (StringUtils.hasLength(keyword)) {
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = driverRepository.searchByKeyword(keyword, pageable);
        } else {
            entityPage = driverRepository.findAll(pageable);
        }

        return getDriverPageResponse(page, size, entityPage);
    }

    @Override
    public DriverPageResponse findAvailableDrivers(String keyword, String sort, int page, int size) {
        Sort.Order order = Sort.Order.asc("id");
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
        keyword = "%" + (keyword != null ? keyword.toLowerCase() : "") + "%";

        Page<Driver> drivers = driverRepository.findAvailableDrivers(keyword, pageable);
        return getDriverPageResponse(page, size, drivers);
    }


    private DriverPageResponse getDriverPageResponse(int page, int size, Page<Driver> driverEntities) {
        log.info("Convert Student Entity Page");

        List<DriverResponse> driverList = driverEntities.stream().map(entity -> DriverResponse.builder()
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

        DriverPageResponse response = new DriverPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(driverEntities.getTotalElements());
        response.setTotalPages(driverEntities.getTotalPages());
        response.setDrivers(driverList);

        return response;
    }

    @Override
    public List<BusScheduleResponse> findScheduleByDate(LocalDate date) {
        UUID driverId = getUserLoggedInId();
        busScheduleRepository.findByDateAndDriver_User_Id(date, driverId);
        List<BusSchedule> busSchedules  = busScheduleRepository.findByDateAndDriver_User_Id(date, driverId);

        busSchedules.sort((o1, o2) -> o2.getDirection().compareTo(o1.getDirection()));

        return busSchedules.stream()
                .map(BusScheduleResponse::new)
                .toList();
    }

    @Override
    public List<BusScheduleResponse> findDriverScheduleByMonth(int year, int month) {
        UUID driverId = getUserLoggedInId();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return busScheduleRepository.findPickUpScheduleByMonthForDriver(startDate, endDate, driverId)
                .stream()
                .map(BusScheduleResponse::new)
                .toList();
    }

}
