package com.example.coffeeordersystem.domain.menu.dto;

import java.time.LocalDateTime;

public interface PopularMenuRow {

    Long getMenuId();

    String getMenuName();

    Long getOrderCount();

    LocalDateTime getLastOrderedAt();
}
