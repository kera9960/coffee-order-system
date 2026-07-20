package com.example.coffeeordersystem.domain.menu.dto;

import com.example.coffeeordersystem.domain.menu.entity.MenuStatus;

import jakarta.validation.constraints.NotNull;

public record ChangeMenuStatusRequest(
        @NotNull
        MenuStatus status
) {
}
