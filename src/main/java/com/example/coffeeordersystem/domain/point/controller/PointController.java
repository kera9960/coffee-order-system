package com.example.coffeeordersystem.domain.point.controller;

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
import com.example.coffeeordersystem.domain.point.dto.ChargePointRequest;
import com.example.coffeeordersystem.domain.point.dto.ChargePointResponse;
import com.example.coffeeordersystem.domain.point.dto.PointBalanceResponse;
import com.example.coffeeordersystem.domain.point.dto.PointTransactionResponse;
import com.example.coffeeordersystem.domain.point.entity.PointTransactionType;
import com.example.coffeeordersystem.domain.point.service.PointService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers/{customerId}/points")
public class PointController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT = "transactedAt,desc";
    private static final List<String> ALLOWED_SORT_PROPERTIES = List.of("id", "amount", "balanceAfter", "transactedAt", "type");

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping("/balance")
    public ApiResponse<PointBalanceResponse> getPointBalance(@PathVariable Long customerId) {
        return ApiResponse.of(pointService.getPointBalance(customerId));
    }

    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<ChargePointResponse>> chargePoint(
            @PathVariable Long customerId,
            @Valid @RequestBody ChargePointRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(pointService.chargePoint(customerId, request)));
    }

    @GetMapping("/transactions")
    public PageApiResponse<PointTransactionResponse> getPointTransactions(
            @PathVariable Long customerId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort
    ) {
        Pageable pageable = toPageable(page, size, sort);
        PointTransactionType transactionType = toTransactionType(type);
        Page<PointTransactionResponse> transactionPage = pointService.getPointTransactions(customerId, transactionType, pageable);
        return PageApiResponse.of(
                transactionPage.getContent(),
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                normalizeSort(sort)
        );
    }

    private PointTransactionType toTransactionType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return PointTransactionType.valueOf(type.trim());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_POINT_TRANSACTION_TYPE);
        }
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
