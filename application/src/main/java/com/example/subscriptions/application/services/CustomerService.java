package com.example.subscriptions.application.services;

import com.example.subscriptions.application.ports.CustomerRepositoryPort;
import com.example.subscriptions.domain.common.BusinessException;
import com.example.subscriptions.domain.model.Customer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {
    private final CustomerRepositoryPort customers;

    public CustomerService(CustomerRepositoryPort customers) {
        this.customers = customers;
    }

    public Customer create(Customer c) {
        customers.findByEmail(c.getEmail()).ifPresent(x -> {
            throw new BusinessException("A Customer with the same email already exists");
        });
        return customers.save(c);
    }

    public Customer findByEmail(String email) {
        return customers.findByEmail(email).orElseThrow(() -> new BusinessException("Customer with email " + email + " does not exist"));
    }
}
