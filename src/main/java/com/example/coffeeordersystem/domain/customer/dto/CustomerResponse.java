package com.example.coffeeordersystem.domain.customer.dto;

import java.time.LocalDateTime;

import com.example.coffeeordersystem.domain.customer.entity.Customer;

public record CustomerResponse(
        Long id,
        String name,
        Long pointBalance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getPointBalance(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
