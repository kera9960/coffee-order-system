package com.example.coffeeordersystem.domain.menu.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.coffeeordersystem.domain.menu.dto.PopularMenuRow;
import com.example.coffeeordersystem.domain.order.entity.Order;

public interface PopularMenuQueryRepository extends Repository<Order, Long> {

    @Query("""
            select m.id as menuId,
                   m.name as menuName,
                   count(distinct o.id) as orderCount,
                   max(o.orderedAt) as lastOrderedAt
            from CoffeeOrder o
            join o.items oi
            join Menu m on m.id = oi.menuId
            where o.status = com.example.coffeeordersystem.domain.order.entity.OrderStatus.COMPLETED
              and m.status = com.example.coffeeordersystem.domain.menu.entity.MenuStatus.ON_SALE
              and o.orderedAt >= :startAt
              and o.orderedAt <= :endAt
            group by m.id, m.name
            order by count(distinct o.id) desc, max(o.orderedAt) desc, m.id asc
            """)
    List<PopularMenuRow> findPopularMenus(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            Pageable pageable
    );
}
