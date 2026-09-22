package com.example.signeasy.application.ports.inbound;

import com.example.signeasy.domain.model.customer.Customer;

public interface CustomerInboundPort {
    Customer create(Customer customer);
    Customer findByEmail(String email);
    void provisionIfNotExists(String sub, String tenantId, String email, String name);
}
