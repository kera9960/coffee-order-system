package com.example.coffeeordersystem.domain.menu.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.common.error.GlobalExceptionHandler;
import com.example.coffeeordersystem.domain.menu.dto.MenuResponse;
import com.example.coffeeordersystem.domain.menu.entity.MenuStatus;
import com.example.coffeeordersystem.domain.menu.service.MenuService;

@WebMvcTest(MenuController.class)
@Import(GlobalExceptionHandler.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @Test
    void createMenuReturnsCreatedAndDataWrapper() throws Exception {
        when(menuService.createMenu(any()))
                .thenReturn(new MenuResponse(1L, "아메리카노", 4500L, MenuStatus.ON_SALE));

        mockMvc.perform(post("/api/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"아메리카노","price":4500}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("아메리카노"))
                .andExpect(jsonPath("$.data.price").value(4500))
                .andExpect(jsonPath("$.data.status").value("ON_SALE"));
    }

    @Test
    void getMenusReturnsPageResponse() throws Exception {
        when(menuService.getOnSaleMenus(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(
                        new MenuResponse(1L, "아메리카노", 4500L, MenuStatus.ON_SALE)
                )));

        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status").value("ON_SALE"))
                .andExpect(jsonPath("$.page.page").value(0))
                .andExpect(jsonPath("$.page.size").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.sort").value("id,asc"));
    }

    @Test
    void getMenuReturnsStoppedMenu() throws Exception {
        when(menuService.getMenu(1L))
                .thenReturn(new MenuResponse(1L, "아메리카노", 4500L, MenuStatus.STOPPED));

        mockMvc.perform(get("/api/menus/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("STOPPED"));
    }

    @Test
    void updateMenuReturnsUpdatedMenu() throws Exception {
        when(menuService.updateMenu(eq(1L), any()))
                .thenReturn(new MenuResponse(1L, "카페라떼", 5000L, MenuStatus.ON_SALE));

        mockMvc.perform(put("/api/menus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"카페라떼","price":5000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("카페라떼"))
                .andExpect(jsonPath("$.data.price").value(5000))
                .andExpect(jsonPath("$.data.status").value("ON_SALE"));
    }

    @Test
    void changeStatusReturnsChangedMenu() throws Exception {
        when(menuService.changeStatus(eq(1L), any()))
                .thenReturn(new MenuResponse(1L, "아메리카노", 4500L, MenuStatus.STOPPED));

        mockMvc.perform(patch("/api/menus/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"STOPPED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("STOPPED"));
    }

    @Test
    void blankNameReturnsInvalidMenuName() throws Exception {
        mockMvc.perform(post("/api/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","price":4500}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MENU_NAME"));
    }

    @Test
    void zeroPriceReturnsInvalidMenuPrice() throws Exception {
        mockMvc.perform(post("/api/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"아메리카노","price":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MENU_PRICE"));
    }

    @Test
    void invalidStatusReturnsInvalidMenuStatus() throws Exception {
        mockMvc.perform(patch("/api/menus/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"UNKNOWN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MENU_STATUS"));
    }

    @Test
    void duplicatedNameReturnsDuplicatedMenuName() throws Exception {
        when(menuService.createMenu(any()))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATED_MENU_NAME));

        mockMvc.perform(post("/api/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"아메리카노","price":4500}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATED_MENU_NAME"));
    }

    @Test
    void notFoundReturnsMenuNotFound() throws Exception {
        when(menuService.getMenu(1L))
                .thenThrow(new BusinessException(ErrorCode.MENU_NOT_FOUND));

        mockMvc.perform(get("/api/menus/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MENU_NOT_FOUND"));
    }

    @Test
    void invalidPageReturnsInvalidPageRequest() throws Exception {
        mockMvc.perform(get("/api/menus?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGE_REQUEST"));
    }
}
