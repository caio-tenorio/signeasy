package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.inbound.CustomerInboundPort;
import com.example.signeasy.application.ports.outbound.CustomerRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.TenantContextOutboundPort;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.customer.CustomerKey;
import com.example.signeasy.domain.model.customer.Customer.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CustomerService implements CustomerInboundPort {
    private final CustomerRepositoryOutboundPort customerRepositoryPort;
    private final TenantContextOutboundPort tenantContext;

    public CustomerService(CustomerRepositoryOutboundPort customerRepositoryPort, TenantContextOutboundPort tenantContext) {
        this.customerRepositoryPort = customerRepositoryPort;
        this.tenantContext = tenantContext;
    }

    @Override
    public Customer create(Customer c) {
        String tenantId = tenantContext.currentTenantId();
        if (c.getKey() == null) {
            c.setKey(new CustomerKey());
        }
        c.getKey().setTenantId(tenantId);
        if (c.getKey().getId() == null) {
            c.getKey().setId(UUID.randomUUID());
        }
        customerRepositoryPort.findByEmailAndTenantId(c.getEmail(), tenantId).ifPresent(x -> {
            throw new BusinessException("A Customer with the same email already exists");
        });
        return customerRepositoryPort.create(c);
    }

    @Override
    public Customer findByEmail(String email) {
        return findByEmailAndTenantId(email, tenantContext.currentTenantId());
    }

    @Override
    public void provisionIfNotExists(String sub, String tenantId, String email, String name) {
        UUID id = UUID.fromString(sub);
        if (customerRepositoryPort.findByIdAndTenantId(id, tenantId).isEmpty()) {
            CustomerKey key = new CustomerKey(id, tenantId);
            Customer customer = new Customer();
            customer.setKey(key);
            customer.setEmail(email);
            customer.setName(name);
            customer.setStatus(Status.ACTIVE);
            customerRepositoryPort.create(customer);
        }
    }

    private Customer findByEmailAndTenantId(String email, String tenantId) {
        return customerRepositoryPort.findByEmailAndTenantId(email, tenantId)
                .orElseThrow(() -> new BusinessException("Customer with email " + email + " does not exist"));
    }
}
