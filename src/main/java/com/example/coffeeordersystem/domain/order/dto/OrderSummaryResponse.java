package com.example.coffeeordersystem.domain.order.dto;

import java.time.LocalDateTime;

import com.example.coffeeordersystem.domain.order.entity.OrderStatus;
import com.example.coffeeordersystem.domain.order.repository.OrderSummaryProjection;

public record OrderSummaryResponse(
        Long id,
        Long customerId,
        Long totalAmount,
        OrderStatus status,
        LocalDateTime orderedAt,
        Long itemCount
) {

    public static OrderSummaryResponse from(OrderSummaryProjection projection) {
        return new OrderSummaryResponse(
                projection.getId(),
                projection.getCustomerId(),
                projection.getTotalAmount(),
                projection.getStatus(),
                projection.getOrderedAt(),
                projection.getItemCount()
        );
    }
}
