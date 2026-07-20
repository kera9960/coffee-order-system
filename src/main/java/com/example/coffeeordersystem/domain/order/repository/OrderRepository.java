package com.example.coffeeordersystem.domain.order.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.coffeeordersystem.domain.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(
            value = """
                    select o.id as id,
                           o.customer.id as customerId,
                           o.totalAmount as totalAmount,
                           o.status as status,
                           o.orderedAt as orderedAt,
                           count(i.id) as itemCount
                    from CoffeeOrder o
                    join o.items i
                    where o.customer.id = :customerId
                    group by o.id, o.customer.id, o.totalAmount, o.status, o.orderedAt
                    """,
            countQuery = "select count(o) from CoffeeOrder o where o.customer.id = :customerId"
    )
    Page<OrderSummaryProjection> findSummariesByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query("""
            select distinct o
            from CoffeeOrder o
            join fetch o.items
            where o.id = :orderId
              and o.customer.id = :customerId
            """)
    Optional<Order> findDetailByIdAndCustomerId(@Param("orderId") Long orderId, @Param("customerId") Long customerId);
}
