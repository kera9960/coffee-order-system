package com.example.coffeeordersystem.domain.order.repository;

import java.time.LocalDateTime;

import com.example.coffeeordersystem.domain.order.entity.OrderStatus;

public interface OrderSummaryProjection {

    Long getId();

    Long getCustomerId();

    Long getTotalAmount();

    OrderStatus getStatus();

    LocalDateTime getOrderedAt();

    Long getItemCount();
}
