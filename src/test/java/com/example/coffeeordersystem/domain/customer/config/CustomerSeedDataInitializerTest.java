package com.example.coffeeordersystem.domain.customer.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.example.coffeeordersystem.domain.customer.entity.Customer;
import com.example.coffeeordersystem.domain.customer.repository.CustomerRepository;

class CustomerSeedDataInitializerTest {

    @Test
    void runCreatesInitialCustomerOnlyWhenRepositoryIsEmpty() {
        CustomerRepository customerRepository = org.mockito.Mockito.mock(CustomerRepository.class);
        when(customerRepository.count()).thenReturn(0L);
        CustomerSeedDataInitializer initializer = new CustomerSeedDataInitializer(customerRepository);

        initializer.run(null);

        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void runDoesNotCreateInitialCustomerWhenRepositoryHasData() {
        CustomerRepository customerRepository = org.mockito.Mockito.mock(CustomerRepository.class);
        when(customerRepository.count()).thenReturn(1L);
        CustomerSeedDataInitializer initializer = new CustomerSeedDataInitializer(customerRepository);

        initializer.run(null);

        verify(customerRepository, never()).save(any(Customer.class));
    }
}
