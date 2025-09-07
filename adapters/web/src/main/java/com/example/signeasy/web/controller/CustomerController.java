package com.example.signeasy.web.controller;

import com.example.signeasy.application.services.CustomerService;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.web.dto.CustomerDtos;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customers;

    public CustomerController(CustomerService customers) {this.customers = customers;}

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
        return customers.findByEmail(email);
    }
}
