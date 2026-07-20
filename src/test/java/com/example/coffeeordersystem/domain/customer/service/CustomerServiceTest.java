package com.example.coffeeordersystem.domain.customer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.domain.customer.dto.CustomerResponse;
import com.example.coffeeordersystem.domain.customer.entity.Customer;
import com.example.coffeeordersystem.domain.customer.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void getCustomersReturnsPagedCustomers() {
        PageRequest pageable = PageRequest.of(0, 20);
        Customer customer = new Customer("홍길동", 12000L);
        when(customerRepository.findAll(pageable)).thenReturn(new PageImpl<>(java.util.List.of(customer), pageable, 1));

        Page<CustomerResponse> response = customerService.getCustomers(pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals("홍길동", response.getContent().get(0).name());
    }

    @Test
    void getCustomerReturnsCustomer() {
        Customer customer = new Customer("홍길동", 12000L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomer(1L);

        assertEquals("홍길동", response.name());
        assertEquals(12000L, response.pointBalance());
    }

    @Test
    void getCustomerFailsWhenCustomerDoesNotExist() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> customerService.getCustomer(1L));

        assertEquals(ErrorCode.CUSTOMER_NOT_FOUND, exception.getErrorCode());
    }
}
