package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.model.dto.response.busSchedule.BusScheduleResponse;
import com.fpt.bbusbe.model.dto.response.assistant.AssistantPageResponse;
import com.fpt.bbusbe.model.dto.response.assistant.AssistantResponse;
import com.fpt.bbusbe.model.entity.Assistant;
import com.fpt.bbusbe.model.entity.BusSchedule;
import com.fpt.bbusbe.repository.BusScheduleRepository;
import com.fpt.bbusbe.repository.AssistantRepository;
import com.fpt.bbusbe.service.AssistantService;
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
@Slf4j(topic = "Assistant-SERVICE")
public class AssistantServiceImpl implements AssistantService {

    private static final String SORT_REGEX_PATTERN = "(\\w+?)(:)(.*)";

    private final BusScheduleRepository busScheduleRepository;
    private final AssistantRepository assistantRepository;


    public AssistantServiceImpl(BusScheduleRepository busScheduleRepository, AssistantRepository assistantRepository) {
        this.busScheduleRepository = busScheduleRepository;
        this.assistantRepository = assistantRepository;
    }

    @Override
    public AssistantPageResponse findAll(String keyword, String sort, int page, int size) {
        // Implement the logic to find all Assistants with pagination and sorting
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

        Page<Assistant> entityPage;

        if (StringUtils.hasLength(keyword)) {
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = assistantRepository.searchByKeyword(keyword, pageable);
        } else {
            entityPage = assistantRepository.findAll(pageable);
        }

        return getAssistantPageResponse(page, size, entityPage);
    }

    @Override
    public AssistantPageResponse findAvailableAssistants(String keyword, String sort, int page, int size) {
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

        Page<Assistant> assistants = assistantRepository.findAvailableAssistants(keyword, pageable);
        return getAssistantPageResponse(page, size, assistants);
    }


    private AssistantPageResponse getAssistantPageResponse(int page, int size, Page<Assistant> AssistantEntities) {
        log.info("Convert Student Entity Page");

        List<AssistantResponse> AssistantList = AssistantEntities.stream().map(entity -> AssistantResponse.builder()
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

        AssistantPageResponse response = new AssistantPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(AssistantEntities.getTotalElements());
        response.setTotalPages(AssistantEntities.getTotalPages());
        response.setAssistants(AssistantList);

        return response;
    }

    @Override
    public List<BusScheduleResponse> findScheduleByDate(LocalDate date) {
        UUID assistantId = getUserLoggedInId();
        busScheduleRepository.findByDateAndAssistant_User_Id(date, assistantId);
        List<BusSchedule> busSchedules  = busScheduleRepository.findByDateAndAssistant_User_Id(date, assistantId);

        busSchedules.sort((o1, o2) -> o2.getDirection().compareTo(o1.getDirection()));

        return busSchedules.stream()
                .map(BusScheduleResponse::new)
                .toList();
    }

    @Override
    public List<BusScheduleResponse> findScheduleByMonth(int year, int month) {
        UUID assistantId = getUserLoggedInId();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return busScheduleRepository.findPickUpScheduleByMonth(startDate, endDate, assistantId)
                .stream()
                .map(BusScheduleResponse::new)
                .toList();
    }

}
