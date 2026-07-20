package com.example.coffeeordersystem.domain.order.dto;

import java.time.LocalDateTime;

import com.example.coffeeordersystem.domain.point.entity.PointTransaction;
import com.example.coffeeordersystem.domain.point.entity.PointTransactionType;

public record OrderPointTransactionResponse(
        Long id,
        PointTransactionType type,
        Long amount,
        Long balanceAfter,
        LocalDateTime transactedAt
) {

    public static OrderPointTransactionResponse from(PointTransaction pointTransaction) {
        return new OrderPointTransactionResponse(
                pointTransaction.getId(),
                pointTransaction.getType(),
                pointTransaction.getAmount(),
                pointTransaction.getBalanceAfter(),
                pointTransaction.getTransactedAt()
        );
    }
}
