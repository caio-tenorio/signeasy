package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.CustomerRepositoryPort;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.customer.CustomerKey;
import com.example.signeasy.domain.model.customer.Customer.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    public void provisionIfNotExists(String sub, String tenantId, String email, String name) {
        UUID id = UUID.fromString(sub);
        if (customers.findByIdAndTenantId(id, tenantId).isEmpty()) {
            CustomerKey key = new CustomerKey(id, tenantId);
            Customer customer = new Customer();
            customer.setKey(key);
            customer.setEmail(email);
            customer.setName(name);
            customer.setStatus(Status.ACTIVE);
            customers.save(customer);
        }
    }
}
