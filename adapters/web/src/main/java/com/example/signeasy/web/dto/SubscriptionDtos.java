package com.example.signeasy.web.dto;

import com.example.signeasy.domain.common.Period;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SubscriptionDtos {
    public record SubscribeRequest(@NotNull UUID customerId, @NotBlank String planType, @NotNull Period period) {
    }

    public record ChangePlanRequest(@NotBlank String newPlanType, @NotNull Period newPeriod) {
    }
}