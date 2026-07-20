package com.example.coffeeordersystem.domain.menu.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.coffeeordersystem.common.dto.ApiResponse;
import com.example.coffeeordersystem.common.dto.PageApiResponse;
import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.domain.menu.dto.ChangeMenuStatusRequest;
import com.example.coffeeordersystem.domain.menu.dto.CreateMenuRequest;
import com.example.coffeeordersystem.domain.menu.dto.MenuResponse;
import com.example.coffeeordersystem.domain.menu.dto.UpdateMenuRequest;
import com.example.coffeeordersystem.domain.menu.service.MenuService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT = "id,asc";
    private static final List<String> ALLOWED_SORT_PROPERTIES = List.of("id", "name", "price", "status");

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(@Valid @RequestBody CreateMenuRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(menuService.createMenu(request)));
    }

    @GetMapping
    public PageApiResponse<MenuResponse> getMenus(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort
    ) {
        Pageable pageable = toPageable(page, size, sort);
        Page<MenuResponse> menuPage = menuService.getOnSaleMenus(pageable);
        return PageApiResponse.of(
                menuPage.getContent(),
                menuPage.getNumber(),
                menuPage.getSize(),
                menuPage.getTotalElements(),
                menuPage.getTotalPages(),
                normalizeSort(sort)
        );
    }

    @GetMapping("/{menuId}")
    public ApiResponse<MenuResponse> getMenu(@PathVariable Long menuId) {
        return ApiResponse.of(menuService.getMenu(menuId));
    }

    @PutMapping("/{menuId}")
    public ApiResponse<MenuResponse> updateMenu(
            @PathVariable Long menuId,
            @Valid @RequestBody UpdateMenuRequest request
    ) {
        return ApiResponse.of(menuService.updateMenu(menuId, request));
    }

    @PatchMapping("/{menuId}/status")
    public ApiResponse<MenuResponse> changeStatus(
            @PathVariable Long menuId,
            @Valid @RequestBody ChangeMenuStatusRequest request
    ) {
        return ApiResponse.of(menuService.changeStatus(menuId, request));
    }

    private Pageable toPageable(int page, int size, String sort) {
        if (page < 0 || size < 1) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        String[] sortParts = normalizeSort(sort).split(",");
        if (sortParts.length != 2 || !ALLOWED_SORT_PROPERTIES.contains(sortParts[0])) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortParts[1]);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        return PageRequest.of(page, size, Sort.by(direction, sortParts[0]));
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }
        return sort.trim().toLowerCase();
    }
}
