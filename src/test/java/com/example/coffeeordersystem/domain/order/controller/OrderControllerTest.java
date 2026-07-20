package com.example.coffeeordersystem.domain.order.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.common.error.GlobalExceptionHandler;
import com.example.coffeeordersystem.domain.order.dto.OrderDetailResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderItemResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderPaymentResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderPointTransactionResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderSummaryResponse;
import com.example.coffeeordersystem.domain.order.entity.OrderStatus;
import com.example.coffeeordersystem.domain.order.service.OrderService;
import com.example.coffeeordersystem.domain.point.entity.PointTransactionType;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrderReturnsCreatedAndOrderResponse() throws Exception {
        LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 15, 10, 40);
        when(orderService.createOrder(eq(1L), any()))
                .thenReturn(new OrderResponse(
                        20L,
                        1L,
                        13500L,
                        OrderStatus.COMPLETED,
                        orderedAt,
                        List.of(
                                new OrderItemResponse(100L, 1L, "아메리카노", 4500L, 2, 9000L),
                                new OrderItemResponse(101L, 2L, "카페라떼", 4500L, 1, 4500L)
                        ),
                        new OrderPaymentResponse(11L, PointTransactionType.USE, -13500L, 3500L)
                ));

        mockMvc.perform(post("/api/customers/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"menuId": 1, "quantity": 2},
                                    {"menuId": 2, "quantity": 1}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(20))
                .andExpect(jsonPath("$.data.customerId").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(13500))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].menuName").value("아메리카노"))
                .andExpect(jsonPath("$.data.items[0].lineAmount").value(9000))
                .andExpect(jsonPath("$.data.pointTransaction.id").value(11))
                .andExpect(jsonPath("$.data.pointTransaction.type").value("USE"))
                .andExpect(jsonPath("$.data.pointTransaction.amount").value(-13500))
                .andExpect(jsonPath("$.data.pointTransaction.balanceAfter").value(3500))
                .andExpect(jsonPath("$.data.pointTransaction.transactedAt").doesNotExist());
    }

    @Test
    void getOrdersReturnsPageResponse() throws Exception {
        LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 15, 10, 40);
        when(orderService.getOrders(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(new OrderSummaryResponse(20L, 1L, 13500L, OrderStatus.COMPLETED, orderedAt, 2L)),
                        PageRequest.of(0, 20),
                        1
                ));

        mockMvc.perform(get("/api/customers/1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(20))
                .andExpect(jsonPath("$.data[0].itemCount").value(2))
                .andExpect(jsonPath("$.page.sort").value("orderedAt,desc"));
    }

    @Test
    void getOrderReturnsDetailResponseWithPointTransactionTransactedAt() throws Exception {
        LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 15, 10, 40);
        when(orderService.getOrder(1L, 20L))
                .thenReturn(new OrderDetailResponse(
                        20L,
                        1L,
                        4500L,
                        OrderStatus.COMPLETED,
                        orderedAt,
                        List.of(new OrderItemResponse(100L, 1L, "아메리카노", 4500L, 1, 4500L)),
                        new OrderPointTransactionResponse(11L, PointTransactionType.USE, -4500L, 7500L, orderedAt)
                ));

        mockMvc.perform(get("/api/customers/1/orders/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(20))
                .andExpect(jsonPath("$.data.items[0].menuPrice").value(4500))
                .andExpect(jsonPath("$.data.pointTransaction.amount").value(-4500))
                .andExpect(jsonPath("$.data.pointTransaction.transactedAt").value("2026-07-15T10:40:00"));
    }

    @Test
    void emptyItemsReturnsEmptyOrderItems() throws Exception {
        mockMvc.perform(post("/api/customers/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMPTY_ORDER_ITEMS"));
    }

    @Test
    void zeroQuantityReturnsInvalidOrderQuantity() throws Exception {
        mockMvc.perform(post("/api/customers/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"menuId":1,"quantity":0}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ORDER_QUANTITY"));
    }

    @Test
    void nullMenuIdReturnsMenuNotFound() throws Exception {
        mockMvc.perform(post("/api/customers/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"menuId":null,"quantity":1}]}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MENU_NOT_FOUND"));
    }

    @Test
    void duplicatedMenuReturnsDuplicatedOrderMenu() throws Exception {
        when(orderService.createOrder(eq(1L), any()))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATED_ORDER_MENU));

        mockMvc.perform(post("/api/customers/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"menuId":1,"quantity":1},{"menuId":1,"quantity":2}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DUPLICATED_ORDER_MENU"));
    }

    @Test
    void stoppedMenuReturnsMenuNotOnSale() throws Exception {
        when(orderService.createOrder(eq(1L), any()))
                .thenThrow(new BusinessException(ErrorCode.MENU_NOT_ON_SALE));

        mockMvc.perform(post("/api/customers/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"menuId":1,"quantity":1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MENU_NOT_ON_SALE"));
    }

    @Test
    void insufficientPointsReturnsInsufficientPoints() throws Exception {
        when(orderService.createOrder(eq(1L), any()))
                .thenThrow(new BusinessException(ErrorCode.INSUFFICIENT_POINTS));

        mockMvc.perform(post("/api/customers/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"menuId":1,"quantity":1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_POINTS"));
    }

    @Test
    void orderNotFoundReturnsOrderNotFound() throws Exception {
        when(orderService.getOrder(1L, 20L))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(get("/api/customers/1/orders/20"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    void invalidPageReturnsInvalidPageRequest() throws Exception {
        mockMvc.perform(get("/api/customers/1/orders?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGE_REQUEST"));
    }
}
