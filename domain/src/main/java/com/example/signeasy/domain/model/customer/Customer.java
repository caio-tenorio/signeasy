package com.example.signeasy.domain.model.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class Customer {

    private CustomerKey key;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    private Status status = Status.ACTIVE;

    public enum Status {ACTIVE, SUSPENDED}

    public Customer() {}

    public Customer(CustomerKey key, String name, String email, Status status) {
        this.key = key;
        this.name = name;
        this.email = email;
        this.status = status;
    }

    public CustomerKey getKey() {
        return key;
    }

    public void setKey(CustomerKey key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
