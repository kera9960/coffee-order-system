package com.example.coffeeordersystem.domain.customer.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CustomerTest {

    @Test
    void createCustomerStoresNameAndPointBalance() {
        Customer customer = new Customer("홍길동", 12000L);

        assertEquals("홍길동", customer.getName());
        assertEquals(12000L, customer.getPointBalance());
    }

    @Test
    void chargePointIncreasesPointBalance() {
        Customer customer = new Customer("홍길동", 12000L);

        customer.chargePoint(5000L);

        assertEquals(17000L, customer.getPointBalance());
    }

    @Test
    void zeroChargeAmountIsInvalid() {
        Customer customer = new Customer("홍길동", 12000L);

        assertThrows(IllegalArgumentException.class, () -> customer.chargePoint(0L));
    }

    @Test
    void negativeInitialPointBalanceIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Customer("홍길동", -1L));
    }
}
