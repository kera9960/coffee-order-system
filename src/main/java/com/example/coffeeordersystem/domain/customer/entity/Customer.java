package com.example.coffeeordersystem.domain.customer.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Long pointBalance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Customer() {
    }

    public Customer(String name, Long pointBalance) {
        validateName(name);
        validateBalance(pointBalance);
        this.name = name;
        this.pointBalance = pointBalance;
    }

    public void chargePoint(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Point charge amount must be positive");
        }
        this.pointBalance += amount;
    }

    public void usePoint(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Point use amount must be positive");
        }
        if (this.pointBalance < amount) {
            throw new IllegalArgumentException("Point balance is insufficient");
        }
        this.pointBalance -= amount;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name must not be blank");
        }
    }

    private void validateBalance(Long pointBalance) {
        if (pointBalance == null || pointBalance < 0) {
            throw new IllegalArgumentException("Point balance must not be negative");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getPointBalance() {
        return pointBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
