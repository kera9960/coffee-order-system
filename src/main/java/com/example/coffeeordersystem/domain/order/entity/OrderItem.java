package com.example.coffeeordersystem.domain.order.entity;

import java.time.LocalDateTime;

import com.example.coffeeordersystem.domain.menu.entity.Menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "order_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_items_order_menu", columnNames = {"order_id", "menu_id"})
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long menuId;

    @Column(nullable = false, length = 100)
    private String menuName;

    @Column(nullable = false)
    private Long menuPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long lineAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected OrderItem() {
    }

    private OrderItem(Long menuId, String menuName, Long menuPrice, Integer quantity) {
        validateMenu(menuId, menuName, menuPrice);
        validateQuantity(quantity);
        this.menuId = menuId;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.quantity = quantity;
        this.lineAmount = menuPrice * quantity;
    }

    public static OrderItem from(Menu menu, Integer quantity) {
        return new OrderItem(menu.getId(), menu.getName(), menu.getPrice(), quantity);
    }

    void assignOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        this.order = order;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    private void validateMenu(Long menuId, String menuName, Long menuPrice) {
        if (menuId == null) {
            throw new IllegalArgumentException("Menu id must not be null");
        }
        if (menuName == null || menuName.isBlank()) {
            throw new IllegalArgumentException("Menu name must not be blank");
        }
        if (menuPrice == null || menuPrice <= 0) {
            throw new IllegalArgumentException("Menu price must be positive");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Long getMenuId() {
        return menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public Long getMenuPrice() {
        return menuPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Long getLineAmount() {
        return lineAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
