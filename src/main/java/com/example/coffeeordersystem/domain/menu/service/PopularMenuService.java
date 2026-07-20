package com.example.coffeeordersystem.domain.menu.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.coffeeordersystem.domain.menu.dto.PopularMenuResponse;
import com.example.coffeeordersystem.domain.menu.repository.PopularMenuQueryRepository;

@Service
public class PopularMenuService {

    private static final int AGGREGATION_DAYS = 7;
    private static final int POPULAR_MENU_LIMIT = 3;

    private final PopularMenuQueryRepository popularMenuQueryRepository;
    private final Clock clock;

    public PopularMenuService(PopularMenuQueryRepository popularMenuQueryRepository, Clock clock) {
        this.popularMenuQueryRepository = popularMenuQueryRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PopularMenuResponse> getPopularMenus() {
        LocalDateTime endAt = LocalDateTime.now(clock);
        LocalDateTime startAt = endAt.minusDays(AGGREGATION_DAYS);

        return popularMenuQueryRepository.findPopularMenus(startAt, endAt, PageRequest.of(0, POPULAR_MENU_LIMIT))
                .stream()
                .map(PopularMenuResponse::from)
                .toList();
    }
}
