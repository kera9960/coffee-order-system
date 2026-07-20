package com.example.coffeeordersystem.domain.customer.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.common.error.GlobalExceptionHandler;
import com.example.coffeeordersystem.domain.customer.dto.CustomerResponse;
import com.example.coffeeordersystem.domain.customer.service.CustomerService;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void getCustomersReturnsPageResponse() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 15, 10, 0);
        when(customerService.getCustomers(any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(new CustomerResponse(1L, "홍길동", 12000L, now, now)),
                        PageRequest.of(0, 20),
                        1
                ));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("홍길동"))
                .andExpect(jsonPath("$.data[0].pointBalance").value(12000))
                .andExpect(jsonPath("$.page.page").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.sort").value("id,asc"));
    }

    @Test
    void getCustomerReturnsDataWrapper() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 15, 10, 0);
        when(customerService.getCustomer(1L))
                .thenReturn(new CustomerResponse(1L, "홍길동", 12000L, now, now));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.pointBalance").value(12000));
    }

    @Test
    void notFoundReturnsCustomerNotFound() throws Exception {
        when(customerService.getCustomer(1L))
                .thenThrow(new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void invalidPageReturnsInvalidPageRequest() throws Exception {
        mockMvc.perform(get("/api/customers?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGE_REQUEST"));
    }
}
