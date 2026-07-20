package com.example.coffeeordersystem.domain.order.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.coffeeordersystem.domain.customer.entity.Customer;
import com.example.coffeeordersystem.domain.menu.entity.Menu;

class OrderTest {

    @Test
    void createOrderSetsCompletedStatusAndTotalAmount() {
        Customer customer = new Customer("홍길동", 12000L);
        Menu americano = menu(1L, "아메리카노", 4500L);
        Menu latte = menu(2L, "카페라떼", 5000L);

        Order order = new Order(customer, List.of(
                OrderItem.from(americano, 2),
                OrderItem.from(latte, 1)
        ));

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(14000L, order.getTotalAmount());
        assertEquals(2, order.getItems().size());
        assertEquals(order, order.getItems().get(0).getOrder());
    }

    @Test
    void createOrderRejectsEmptyItems() {
        Customer customer = new Customer("홍길동", 12000L);

        assertThrows(IllegalArgumentException.class, () -> new Order(customer, List.of()));
    }

    private Menu menu(Long id, String name, Long price) {
        Menu menu = new Menu(name, price);
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }
}
