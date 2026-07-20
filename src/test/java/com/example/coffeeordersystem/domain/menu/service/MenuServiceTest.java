package com.example.coffeeordersystem.domain.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.domain.menu.dto.ChangeMenuStatusRequest;
import com.example.coffeeordersystem.domain.menu.dto.CreateMenuRequest;
import com.example.coffeeordersystem.domain.menu.dto.MenuResponse;
import com.example.coffeeordersystem.domain.menu.dto.UpdateMenuRequest;
import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.entity.MenuStatus;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void createMenuChecksDuplicateNameAndSavesOnSaleMenu() {
        when(menuRepository.existsByName("아메리카노")).thenReturn(false);
        when(menuRepository.saveAndFlush(any(Menu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MenuResponse response = menuService.createMenu(new CreateMenuRequest("아메리카노", 4500L));

        ArgumentCaptor<Menu> menuCaptor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).saveAndFlush(menuCaptor.capture());
        assertEquals("아메리카노", response.name());
        assertEquals(4500L, response.price());
        assertEquals(MenuStatus.ON_SALE, menuCaptor.getValue().getStatus());
    }

    @Test
    void createMenuFailsWhenNameAlreadyExists() {
        when(menuRepository.existsByName("아메리카노")).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> menuService.createMenu(new CreateMenuRequest("아메리카노", 4500L))
        );

        assertEquals(ErrorCode.DUPLICATED_MENU_NAME, exception.getErrorCode());
    }

    @Test
    void createMenuConvertsUniqueViolationToDuplicatedMenuName() {
        when(menuRepository.existsByName("아메리카노")).thenReturn(false);
        when(menuRepository.saveAndFlush(any(Menu.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> menuService.createMenu(new CreateMenuRequest("아메리카노", 4500L))
        );

        assertEquals(ErrorCode.DUPLICATED_MENU_NAME, exception.getErrorCode());
    }

    @Test
    void getOnSaleMenusQueriesRepositoryWithOnSaleStatus() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(menuRepository.findAllByStatus(eq(MenuStatus.ON_SALE), eq(pageable))).thenReturn(Page.empty(pageable));

        menuService.getOnSaleMenus(pageable);

        verify(menuRepository).findAllByStatus(MenuStatus.ON_SALE, pageable);
    }

    @Test
    void getMenuReturnsStoppedMenuToo() {
        Menu menu = new Menu("아메리카노", 4500L);
        menu.changeStatus(MenuStatus.STOPPED);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        MenuResponse response = menuService.getMenu(1L);

        assertEquals(MenuStatus.STOPPED, response.status());
    }

    @Test
    void updateMenuKeepsStatusAndChangesNameAndPrice() {
        Menu menu = new Menu("아메리카노", 4500L);
        menu.changeStatus(MenuStatus.STOPPED);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(menuRepository.existsByNameAndIdNot("카페라떼", 1L)).thenReturn(false);
        when(menuRepository.saveAndFlush(menu)).thenReturn(menu);

        MenuResponse response = menuService.updateMenu(1L, new UpdateMenuRequest("카페라떼", 5000L));

        assertEquals("카페라떼", response.name());
        assertEquals(5000L, response.price());
        assertEquals(MenuStatus.STOPPED, response.status());
    }

    @Test
    void changeStatusAllowsSameStatusRequest() {
        Menu menu = new Menu("아메리카노", 4500L);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        MenuResponse response = menuService.changeStatus(1L, new ChangeMenuStatusRequest(MenuStatus.ON_SALE));

        assertEquals(MenuStatus.ON_SALE, response.status());
    }

    @Test
    void getMenuFailsWhenMenuDoesNotExist() {
        when(menuRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> menuService.getMenu(1L));

        assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
    }
}
