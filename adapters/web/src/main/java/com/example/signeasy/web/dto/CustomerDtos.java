package com.example.signeasy.web.dto;

import com.example.signeasy.web.dto.ApiTypes.CustomerStatus;

public class CustomerDtos {
    public record CustomerResponse(KeyResponse key, String name, String email, CustomerStatus status) {}

    public record CreateCustomerRequest(String name, String email) {

    }
}
