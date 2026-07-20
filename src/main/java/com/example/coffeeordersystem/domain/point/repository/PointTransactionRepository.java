package com.example.coffeeordersystem.domain.point.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.coffeeordersystem.domain.point.entity.PointTransaction;
import com.example.coffeeordersystem.domain.point.entity.PointTransactionType;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    Page<PointTransaction> findAllByCustomerId(Long customerId, Pageable pageable);

    Page<PointTransaction> findAllByCustomerIdAndType(Long customerId, PointTransactionType type, Pageable pageable);

    Optional<PointTransaction> findByOrderId(Long orderId);
}
