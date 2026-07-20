package com.example.coffeeordersystem.domain.menu.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MenuTest {

    @Test
    void newMenuStatusIsOnSale() {
        Menu menu = new Menu("아메리카노", 4500L);

        assertEquals(MenuStatus.ON_SALE, menu.getStatus());
    }

    @Test
    void updateInfoChangesNameAndPrice() {
        Menu menu = new Menu("아메리카노", 4500L);

        menu.updateInfo("카페라떼", 5000L);

        assertEquals("카페라떼", menu.getName());
        assertEquals(5000L, menu.getPrice());
    }

    @Test
    void changeStatusAllowsStoppedAndOnSale() {
        Menu menu = new Menu("아메리카노", 4500L);

        menu.changeStatus(MenuStatus.STOPPED);
        menu.changeStatus(MenuStatus.ON_SALE);

        assertEquals(MenuStatus.ON_SALE, menu.getStatus());
    }

    @Test
    void blankNameIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Menu(" ", 4500L));
    }

    @Test
    void zeroPriceIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Menu("아메리카노", 0L));
    }
}
