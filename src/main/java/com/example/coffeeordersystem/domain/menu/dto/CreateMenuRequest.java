package com.example.coffeeordersystem.domain.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateMenuRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @Positive
        Long price
) {
}
