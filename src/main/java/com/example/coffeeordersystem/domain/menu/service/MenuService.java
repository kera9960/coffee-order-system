package com.example.coffeeordersystem.domain.menu.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.domain.menu.dto.ChangeMenuStatusRequest;
import com.example.coffeeordersystem.domain.menu.dto.CreateMenuRequest;
import com.example.coffeeordersystem.domain.menu.dto.MenuResponse;
import com.example.coffeeordersystem.domain.menu.dto.UpdateMenuRequest;
import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.entity.MenuStatus;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Transactional
    public MenuResponse createMenu(CreateMenuRequest request) {
        validateNotDuplicatedName(request.name());
        return saveAndMap(new Menu(request.name(), request.price()));
    }

    @Transactional(readOnly = true)
    public Page<MenuResponse> getOnSaleMenus(Pageable pageable) {
        return menuRepository.findAllByStatus(MenuStatus.ON_SALE, pageable)
                .map(MenuResponse::from);
    }

    @Transactional(readOnly = true)
    public MenuResponse getMenu(Long menuId) {
        return MenuResponse.from(findMenu(menuId));
    }

    @Transactional
    public MenuResponse updateMenu(Long menuId, UpdateMenuRequest request) {
        Menu menu = findMenu(menuId);
        validateNotDuplicatedNameForUpdate(request.name(), menuId);
        menu.updateInfo(request.name(), request.price());
        return saveAndMap(menu);
    }

    @Transactional
    public MenuResponse changeStatus(Long menuId, ChangeMenuStatusRequest request) {
        Menu menu = findMenu(menuId);
        menu.changeStatus(request.status());
        return MenuResponse.from(menu);
    }

    private Menu findMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    private void validateNotDuplicatedName(String name) {
        if (menuRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.DUPLICATED_MENU_NAME);
        }
    }

    private void validateNotDuplicatedNameForUpdate(String name, Long menuId) {
        if (menuRepository.existsByNameAndIdNot(name, menuId)) {
            throw new BusinessException(ErrorCode.DUPLICATED_MENU_NAME);
        }
    }

    private MenuResponse saveAndMap(Menu menu) {
        try {
            return MenuResponse.from(menuRepository.saveAndFlush(menu));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.DUPLICATED_MENU_NAME);
        }
    }
}
