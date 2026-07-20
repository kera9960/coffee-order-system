package com.example.coffeeordersystem.domain.order.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.coffeeordersystem.domain.menu.entity.Menu;

class OrderItemTest {

    @Test
    void fromStoresMenuSnapshotAndCalculatesLineAmount() {
        Menu menu = new Menu("아메리카노", 4500L);
        ReflectionTestUtils.setField(menu, "id", 1L);

        OrderItem item = OrderItem.from(menu, 2);

        assertEquals(1L, item.getMenuId());
        assertEquals("아메리카노", item.getMenuName());
        assertEquals(4500L, item.getMenuPrice());
        assertEquals(2, item.getQuantity());
        assertEquals(9000L, item.getLineAmount());
    }

    @Test
    void zeroQuantityIsInvalid() {
        Menu menu = new Menu("아메리카노", 4500L);
        ReflectionTestUtils.setField(menu, "id", 1L);

        assertThrows(IllegalArgumentException.class, () -> OrderItem.from(menu, 0));
    }
}
