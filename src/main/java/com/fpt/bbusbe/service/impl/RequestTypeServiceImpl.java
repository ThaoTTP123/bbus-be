package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.model.dto.response.requestType.RequestTypePageResponse;
import com.fpt.bbusbe.model.dto.response.requestType.RequestTypeResponse;
import com.fpt.bbusbe.model.entity.RequestType;
import com.fpt.bbusbe.repository.RequestTypeRepository;
import com.fpt.bbusbe.service.RequestTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j(topic = "CHECKPOINT_SERVICE")
@RequiredArgsConstructor
public class RequestTypeServiceImpl implements RequestTypeService {

    private final RequestTypeRepository requestTypeRepository;

    @Override
    public RequestTypePageResponse findAll(String keyword, String sort, int page, int size) {
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

        // Nếu không có roleName, truyền `null` để bỏ qua lọc theo role

        Page<RequestType> entityPage = requestTypeRepository.searchByKeyword(formattedKeyword, pageable);
        return getRequestTypePageResponse(page, size, entityPage);
    }

    @Override
    public RequestTypeResponse findById(UUID id) {
        RequestType request = getRequestTypeEntity(id);
        return RequestTypeResponse.builder()
                .requestTypeId(request.getId())
                .requestTypeName(request.getRequestTypeName())
                .build();
    }

    private RequestType getRequestTypeEntity(UUID id) {
        return requestTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    }

    /**
     * Convert Request Entity Page
     * @param page
     * @param size
     * @param requestEntities
     * @return
     */
    private static RequestTypePageResponse getRequestTypePageResponse(int page, int size, Page<RequestType> requestEntities) {
        log.info("Convert Request Entity Page");

        List<RequestTypeResponse> requestList = requestEntities.stream().map(entity -> RequestTypeResponse.builder()
                .requestTypeId(entity.getId())
                .requestTypeName(entity.getRequestTypeName())
                .build()
        ).toList();

        RequestTypePageResponse response = new RequestTypePageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(requestEntities.getTotalElements());
        response.setTotalPages(requestEntities.getTotalPages());
        response.setRequestTypes(requestList);

        return response;
    }
}
