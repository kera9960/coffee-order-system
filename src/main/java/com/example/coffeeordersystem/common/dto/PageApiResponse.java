package com.example.coffeeordersystem.common.dto;

import java.util.List;

public record PageApiResponse<T>(
        List<T> data,
        PageInfo page
) {

    public static <T> PageApiResponse<T> of(
            List<T> data,
            int page,
            int size,
            long totalElements,
            int totalPages,
            String sort
    ) {
        return new PageApiResponse<>(
                data,
                new PageInfo(page, size, totalElements, totalPages, sort)
        );
    }

    public record PageInfo(
            int page,
            int size,
            long totalElements,
            int totalPages,
            String sort
    ) {
    }
}
