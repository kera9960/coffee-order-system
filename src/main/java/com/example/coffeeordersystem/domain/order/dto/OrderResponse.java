package com.example.coffeeordersystem.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderStatus;
import com.example.coffeeordersystem.domain.point.entity.PointTransaction;

public record OrderResponse(
        Long id,
        Long customerId,
        Long totalAmount,
        OrderStatus status,
        LocalDateTime orderedAt,
        List<OrderItemResponse> items,
        OrderPaymentResponse pointTransaction
) {

    public static OrderResponse of(Order order, PointTransaction pointTransaction) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderedAt(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                OrderPaymentResponse.from(pointTransaction)
        );
    }
}
