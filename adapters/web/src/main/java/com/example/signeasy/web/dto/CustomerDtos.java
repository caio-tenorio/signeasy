package com.example.signeasy.web.dto;

import jakarta.validation.Valid;

public class CustomerDtos {
    public record CreateCustomerRequest(String name, String email) {

    }
}
