package com.example.coffeeordersystem.domain.order.dto;

import com.example.coffeeordersystem.domain.order.entity.OrderItem;

public record OrderItemResponse(
        Long id,
        Long menuId,
        String menuName,
        Long menuPrice,
        Integer quantity,
        Long lineAmount
) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getMenuId(),
                item.getMenuName(),
                item.getMenuPrice(),
                item.getQuantity(),
                item.getLineAmount()
        );
    }
}
