package com.example.coffeeordersystem.domain.order.dto;

import com.example.coffeeordersystem.domain.point.entity.PointTransaction;
import com.example.coffeeordersystem.domain.point.entity.PointTransactionType;

public record OrderPaymentResponse(
        Long id,
        PointTransactionType type,
        Long amount,
        Long balanceAfter
) {

    public static OrderPaymentResponse from(PointTransaction pointTransaction) {
        return new OrderPaymentResponse(
                pointTransaction.getId(),
                pointTransaction.getType(),
                pointTransaction.getAmount(),
                pointTransaction.getBalanceAfter()
        );
    }
}
