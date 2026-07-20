package com.example.coffeeordersystem.domain.point.dto;

import java.time.LocalDateTime;

import com.example.coffeeordersystem.domain.point.entity.PointTransaction;
import com.example.coffeeordersystem.domain.point.entity.PointTransactionType;

public record PointTransactionResponse(
        Long id,
        Long customerId,
        Long orderId,
        PointTransactionType type,
        Long amount,
        Long balanceAfter,
        LocalDateTime transactedAt
) {

    public static PointTransactionResponse from(PointTransaction pointTransaction) {
        Long transactionId = pointTransaction.getId();
        return new PointTransactionResponse(
                transactionId,
                pointTransaction.getCustomer().getId(),
                pointTransaction.getOrderId(),
                pointTransaction.getType(),
                pointTransaction.getAmount(),
                pointTransaction.getBalanceAfter(),
                pointTransaction.getTransactedAt()
        );
    }
}
