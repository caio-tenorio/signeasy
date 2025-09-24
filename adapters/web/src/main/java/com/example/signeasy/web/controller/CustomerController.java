package com.example.signeasy.web.controller;

import com.example.signeasy.application.services.CustomerService;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.web.dto.CustomerDtos;
import com.example.signeasy.web.security.TenantProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customers;
    private final TenantProvider tenantProvider;

    public CustomerController(CustomerService customers, TenantProvider tenantProvider) {
        this.customers = customers;
        this.tenantProvider = tenantProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Customer create(@RequestBody @Validated CustomerDtos.CreateCustomerRequest req) {
        var c = new Customer();
        c.setName(req.name());
        c.setEmail(req.email());
        return customers.create(c);
    }

    @GetMapping
    public Customer findByEmail(@RequestParam("email") String email) {
        return customers.findByEmailAndTenantId(email, tenantProvider.getCurrentTenantId());
    }
}
