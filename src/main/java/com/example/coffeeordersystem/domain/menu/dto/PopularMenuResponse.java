package com.example.coffeeordersystem.domain.menu.dto;

public record PopularMenuResponse(
        Long menuId,
        String menuName,
        Long orderCount
) {

    public static PopularMenuResponse from(PopularMenuRow row) {
        return new PopularMenuResponse(row.getMenuId(), row.getMenuName(), row.getOrderCount());
    }
}
