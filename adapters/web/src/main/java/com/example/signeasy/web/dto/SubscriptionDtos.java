package com.example.signeasy.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SubscriptionDtos {
    public record SubscribeRequest(@NotNull UUID customerId, @NotBlank String planType) {
    }

    public record ChangePlanRequest(@NotBlank String newPlanType) {
    }
}