package com.example.coffeeordersystem.domain.menu.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.coffeeordersystem.domain.menu.dto.PopularMenuRow;

class PopularMenuQueryRepositoryTest {

    @Test
    void findPopularMenusQueryContainsAggregationRules() throws Exception {
        Method method = PopularMenuQueryRepository.class.getMethod(
                "findPopularMenus",
                LocalDateTime.class,
                LocalDateTime.class,
                Pageable.class
        );

        Query query = method.getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value())
                .contains("count(distinct o.id)")
                .contains("max(o.orderedAt)")
                .contains("OrderStatus.COMPLETED")
                .contains("MenuStatus.ON_SALE")
                .contains("o.orderedAt >= :startAt")
                .contains("o.orderedAt <= :endAt")
                .contains("order by count(distinct o.id) desc, max(o.orderedAt) desc, m.id asc");
    }

    @Test
    void findPopularMenusReturnsPopularMenuRowProjectionList() throws Exception {
        Method method = PopularMenuQueryRepository.class.getMethod(
                "findPopularMenus",
                LocalDateTime.class,
                LocalDateTime.class,
                Pageable.class
        );

        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(PopularMenuRow.class.getMethod("getMenuId").getReturnType()).isEqualTo(Long.class);
        assertThat(PopularMenuRow.class.getMethod("getMenuName").getReturnType()).isEqualTo(String.class);
        assertThat(PopularMenuRow.class.getMethod("getOrderCount").getReturnType()).isEqualTo(Long.class);
        assertThat(PopularMenuRow.class.getMethod("getLastOrderedAt").getReturnType()).isEqualTo(LocalDateTime.class);
    }
}
