package com.example.coffeeordersystem.domain.customer.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.coffeeordersystem.domain.customer.entity.Customer;
import com.example.coffeeordersystem.domain.customer.repository.CustomerRepository;

@Profile("local")
@Component
public class CustomerSeedDataInitializer implements ApplicationRunner {

    private final CustomerRepository customerRepository;

    public CustomerSeedDataInitializer(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (customerRepository.count() > 0) {
            return;
        }
        customerRepository.save(new Customer("홍길동", 12000L));
    }
}
