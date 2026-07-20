package com.example.coffeeordersystem.domain.point.entity;

import java.time.LocalDateTime;

import com.example.coffeeordersystem.domain.customer.entity.Customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "point_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_point_transactions_order_id", columnNames = "order_id"),
        indexes = @Index(name = "idx_point_transactions_customer_transacted_at", columnList = "customer_id, transacted_at")
)
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "order_id")
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long balanceAfter;

    @Column(nullable = false)
    private LocalDateTime transactedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PointTransaction() {
    }

    private PointTransaction(
            Customer customer,
            Long orderId,
            PointTransactionType type,
            Long amount,
            Long balanceAfter
    ) {
        validateCustomer(customer);
        validateBalanceAfter(balanceAfter);
        this.customer = customer;
        this.orderId = orderId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    public static PointTransaction createCharge(Customer customer, Long amount, Long balanceAfter) {
        validateChargeAmount(amount);
        return new PointTransaction(customer, null, PointTransactionType.CHARGE, amount, balanceAfter);
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.transactedAt = now;
        this.createdAt = now;
    }

    private static void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }
    }

    private static void validateChargeAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Charge amount must be positive");
        }
    }

    private static void validateBalanceAfter(Long balanceAfter) {
        if (balanceAfter == null || balanceAfter < 0) {
            throw new IllegalArgumentException("Balance after must not be negative");
        }
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Long getOrderId() {
        return orderId;
    }

    public PointTransactionType getType() {
        return type;
    }

    public Long getAmount() {
        return amount;
    }

    public Long getBalanceAfter() {
        return balanceAfter;
    }

    public LocalDateTime getTransactedAt() {
        return transactedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
