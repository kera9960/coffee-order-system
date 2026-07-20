package com.example.coffeeordersystem.domain.point.controller;

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
import com.example.coffeeordersystem.domain.point.dto.ChargePointResponse;
import com.example.coffeeordersystem.domain.point.dto.PointBalanceResponse;
import com.example.coffeeordersystem.domain.point.dto.PointTransactionResponse;
import com.example.coffeeordersystem.domain.point.entity.PointTransactionType;
import com.example.coffeeordersystem.domain.point.service.PointService;

@WebMvcTest(PointController.class)
@Import(GlobalExceptionHandler.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PointService pointService;

    @Test
    void getPointBalanceReturnsDataWrapper() throws Exception {
        when(pointService.getPointBalance(1L))
                .thenReturn(new PointBalanceResponse(1L, 12000L));

        mockMvc.perform(get("/api/customers/1/points/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerId").value(1))
                .andExpect(jsonPath("$.data.pointBalance").value(12000));
    }

    @Test
    void chargePointReturnsCreatedAndTransactionId() throws Exception {
        LocalDateTime transactedAt = LocalDateTime.of(2026, 7, 15, 10, 30);
        when(pointService.chargePoint(eq(1L), any()))
                .thenReturn(new ChargePointResponse(
                        10L,
                        1L,
                        null,
                        PointTransactionType.CHARGE,
                        5000L,
                        17000L,
                        transactedAt
                ));

        mockMvc.perform(post("/api/customers/1/points/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":5000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transactionId").value(10))
                .andExpect(jsonPath("$.data.customerId").value(1))
                .andExpect(jsonPath("$.data.orderId").doesNotExist())
                .andExpect(jsonPath("$.data.type").value("CHARGE"))
                .andExpect(jsonPath("$.data.amount").value(5000))
                .andExpect(jsonPath("$.data.balanceAfter").value(17000));
    }

    @Test
    void getPointTransactionsReturnsPageResponse() throws Exception {
        LocalDateTime transactedAt = LocalDateTime.of(2026, 7, 15, 10, 30);
        when(pointService.getPointTransactions(eq(1L), eq(PointTransactionType.CHARGE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(new PointTransactionResponse(
                                10L,
                                1L,
                                null,
                                PointTransactionType.CHARGE,
                                5000L,
                                17000L,
                                transactedAt
                        )),
                        PageRequest.of(0, 20),
                        1
                ));

        mockMvc.perform(get("/api/customers/1/points/transactions?type=CHARGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].customerId").value(1))
                .andExpect(jsonPath("$.data[0].orderId").doesNotExist())
                .andExpect(jsonPath("$.data[0].type").value("CHARGE"))
                .andExpect(jsonPath("$.data[0].amount").value(5000))
                .andExpect(jsonPath("$.data[0].balanceAfter").value(17000))
                .andExpect(jsonPath("$.page.page").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.sort").value("transactedAt,desc"));
    }

    @Test
    void zeroChargeAmountReturnsInvalidPointAmount() throws Exception {
        mockMvc.perform(post("/api/customers/1/points/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_POINT_AMOUNT"));
    }

    @Test
    void invalidTransactionTypeReturnsInvalidPointTransactionType() throws Exception {
        mockMvc.perform(get("/api/customers/1/points/transactions?type=UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_POINT_TRANSACTION_TYPE"));
    }

    @Test
    void invalidPageReturnsInvalidPageRequest() throws Exception {
        mockMvc.perform(get("/api/customers/1/points/transactions?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGE_REQUEST"));
    }

    @Test
    void notFoundReturnsCustomerNotFound() throws Exception {
        when(pointService.getPointBalance(1L))
                .thenThrow(new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        mockMvc.perform(get("/api/customers/1/points/balance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }
}
