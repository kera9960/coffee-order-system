package com.example.coffeeordersystem.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull
        Long menuId,

        @NotNull
        @Min(1)
        Integer quantity
) {
}
