package com.example.coffeeordersystem.domain.point.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.example.coffeeordersystem.domain.customer.entity.Customer;

class PointTransactionTest {

    @Test
    void createChargeStoresPositiveAmountNullOrderIdAndBalanceAfter() {
        Customer customer = new Customer("홍길동", 17000L);

        PointTransaction transaction = PointTransaction.createCharge(customer, 5000L, 17000L);

        assertEquals(customer, transaction.getCustomer());
        assertEquals(PointTransactionType.CHARGE, transaction.getType());
        assertEquals(5000L, transaction.getAmount());
        assertEquals(17000L, transaction.getBalanceAfter());
        assertNull(transaction.getOrderId());
    }

    @Test
    void createChargeRejectsZeroAmount() {
        Customer customer = new Customer("홍길동", 12000L);

        assertThrows(IllegalArgumentException.class, () -> PointTransaction.createCharge(customer, 0L, 12000L));
    }

    @Test
    void createChargeRejectsNegativeBalanceAfter() {
        Customer customer = new Customer("홍길동", 12000L);

        assertThrows(IllegalArgumentException.class, () -> PointTransaction.createCharge(customer, 5000L, -1L));
    }

    @Test
    void createUseStoresNegativeAmountOrderIdAndBalanceAfter() {
        Customer customer = new Customer("홍길동", 7000L);

        PointTransaction transaction = PointTransaction.createUse(customer, 20L, 5000L, 7000L);

        assertEquals(customer, transaction.getCustomer());
        assertEquals(20L, transaction.getOrderId());
        assertEquals(PointTransactionType.USE, transaction.getType());
        assertEquals(-5000L, transaction.getAmount());
        assertEquals(7000L, transaction.getBalanceAfter());
    }

    @Test
    void createUseRejectsNullOrderId() {
        Customer customer = new Customer("홍길동", 7000L);

        assertThrows(IllegalArgumentException.class, () -> PointTransaction.createUse(customer, null, 5000L, 7000L));
    }

    @Test
    void createUseRejectsZeroAmount() {
        Customer customer = new Customer("홍길동", 7000L);

        assertThrows(IllegalArgumentException.class, () -> PointTransaction.createUse(customer, 20L, 0L, 7000L));
    }
}
