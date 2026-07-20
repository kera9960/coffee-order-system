package com.example.coffeeordersystem.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderStatus;
import com.example.coffeeordersystem.domain.point.entity.PointTransaction;

public record OrderDetailResponse(
        Long id,
        Long customerId,
        Long totalAmount,
        OrderStatus status,
        LocalDateTime orderedAt,
        List<OrderItemResponse> items,
        OrderPointTransactionResponse pointTransaction
) {

    public static OrderDetailResponse of(Order order, PointTransaction pointTransaction) {
        return new OrderDetailResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderedAt(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                OrderPointTransactionResponse.from(pointTransaction)
        );
    }
}
