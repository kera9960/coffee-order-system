package com.example.coffeeordersystem.domain.customer.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.coffeeordersystem.common.dto.ApiResponse;
import com.example.coffeeordersystem.common.dto.PageApiResponse;
import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.domain.customer.dto.CustomerResponse;
import com.example.coffeeordersystem.domain.customer.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT = "id,asc";
    private static final List<String> ALLOWED_SORT_PROPERTIES = List.of("id", "name", "pointBalance", "createdAt", "updatedAt");

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public PageApiResponse<CustomerResponse> getCustomers(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort
    ) {
        Pageable pageable = toPageable(page, size, sort);
        Page<CustomerResponse> customerPage = customerService.getCustomers(pageable);
        return PageApiResponse.of(
                customerPage.getContent(),
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages(),
                normalizeSort(sort)
        );
    }

    @GetMapping("/{customerId}")
    public ApiResponse<CustomerResponse> getCustomer(@PathVariable Long customerId) {
        return ApiResponse.of(customerService.getCustomer(customerId));
    }

    private Pageable toPageable(int page, int size, String sort) {
        if (page < 0 || size < 1) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        String[] sortParts = normalizeSort(sort).split(",");
        if (sortParts.length != 2 || !ALLOWED_SORT_PROPERTIES.contains(sortParts[0])) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortParts[1]);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        return PageRequest.of(page, size, Sort.by(direction, sortParts[0]));
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }
        String[] sortParts = sort.trim().split(",");
        if (sortParts.length != 2) {
            return sort.trim();
        }
        return sortParts[0].trim() + "," + sortParts[1].trim().toLowerCase();
    }
}
