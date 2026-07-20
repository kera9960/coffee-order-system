package com.example.coffeeordersystem.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.example.coffeeordersystem.domain.menu.dto.PopularMenuResponse;
import com.example.coffeeordersystem.domain.menu.dto.PopularMenuRow;
import com.example.coffeeordersystem.domain.menu.repository.PopularMenuQueryRepository;

@ExtendWith(MockitoExtension.class)
class PopularMenuServiceTest {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-07-20T03:00:00Z"), SERVICE_ZONE);

    @Mock
    private PopularMenuQueryRepository popularMenuQueryRepository;

    private PopularMenuService popularMenuService;

    @BeforeEach
    void setUp() {
        popularMenuService = new PopularMenuService(popularMenuQueryRepository, FIXED_CLOCK);
    }

    @Test
    void getPopularMenusCalculatesRecentSevenDaysRangeWithInjectedClock() {
        LocalDateTime endAt = LocalDateTime.of(2026, 7, 20, 12, 0);
        LocalDateTime startAt = LocalDateTime.of(2026, 7, 13, 12, 0);
        when(popularMenuQueryRepository.findPopularMenus(eq(startAt), eq(endAt), eq(PageRequest.of(0, 3))))
                .thenReturn(List.of(new TestPopularMenuRow(1L, "아메리카노", 5L, endAt)));

        List<PopularMenuResponse> responses = popularMenuService.getPopularMenus();

        assertThat(responses).containsExactly(new PopularMenuResponse(1L, "아메리카노", 5L));
        verify(popularMenuQueryRepository).findPopularMenus(startAt, endAt, PageRequest.of(0, 3));
    }

    @Test
    void getPopularMenusReturnsEmptyListWhenRepositoryReturnsEmptyList() {
        LocalDateTime endAt = LocalDateTime.of(2026, 7, 20, 12, 0);
        LocalDateTime startAt = LocalDateTime.of(2026, 7, 13, 12, 0);
        when(popularMenuQueryRepository.findPopularMenus(eq(startAt), eq(endAt), eq(PageRequest.of(0, 3))))
                .thenReturn(List.of());

        List<PopularMenuResponse> responses = popularMenuService.getPopularMenus();

        assertThat(responses).isEmpty();
        verify(popularMenuQueryRepository).findPopularMenus(startAt, endAt, PageRequest.of(0, 3));
    }

    private record TestPopularMenuRow(
            Long menuId,
            String menuName,
            Long orderCount,
            LocalDateTime lastOrderedAt
    ) implements PopularMenuRow {

        @Override
        public Long getMenuId() {
            return menuId;
        }

        @Override
        public String getMenuName() {
            return menuName;
        }

        @Override
        public Long getOrderCount() {
            return orderCount;
        }

        @Override
        public LocalDateTime getLastOrderedAt() {
            return lastOrderedAt;
        }
    }
}
