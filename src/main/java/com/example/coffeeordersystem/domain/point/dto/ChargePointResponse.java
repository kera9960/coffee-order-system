package com.example.coffeeordersystem.domain.point.dto;

import java.time.LocalDateTime;

import com.example.coffeeordersystem.domain.point.entity.PointTransaction;
import com.example.coffeeordersystem.domain.point.entity.PointTransactionType;

public record ChargePointResponse(
        Long transactionId,
        Long customerId,
        Long orderId,
        PointTransactionType type,
        Long amount,
        Long balanceAfter,
        LocalDateTime transactedAt
) {

    public static ChargePointResponse from(PointTransaction pointTransaction) {
        return new ChargePointResponse(
                pointTransaction.getId(),
                pointTransaction.getCustomer().getId(),
                pointTransaction.getOrderId(),
                pointTransaction.getType(),
                pointTransaction.getAmount(),
                pointTransaction.getBalanceAfter(),
                pointTransaction.getTransactedAt()
        );
    }
}
