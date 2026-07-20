package com.example.coffeeordersystem.domain.order.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.coffeeordersystem.common.dto.ApiResponse;
import com.example.coffeeordersystem.common.dto.PageApiResponse;
import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.domain.order.dto.CreateOrderRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderDetailResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderSummaryResponse;
import com.example.coffeeordersystem.domain.order.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers/{customerId}/orders")
public class OrderController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT = "orderedAt,desc";
    private static final List<String> ALLOWED_SORT_PROPERTIES = List.of("id", "totalAmount", "status", "orderedAt");

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @PathVariable Long customerId,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(orderService.createOrder(customerId, request)));
    }

    @GetMapping
    public PageApiResponse<OrderSummaryResponse> getOrders(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort
    ) {
        Pageable pageable = toPageable(page, size, sort);
        Page<OrderSummaryResponse> orderPage = orderService.getOrders(customerId, pageable);
        return PageApiResponse.of(
                orderPage.getContent(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                normalizeSort(sort)
        );
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrder(
            @PathVariable Long customerId,
            @PathVariable Long orderId
    ) {
        return ApiResponse.of(orderService.getOrder(customerId, orderId));
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
