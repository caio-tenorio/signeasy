package com.example.signeasy.web.dto;

import com.example.signeasy.web.dto.ApiTypes.Period;
import com.example.signeasy.web.dto.ApiTypes.SubscriptionStatus;
import com.example.signeasy.web.dto.CustomerDtos.CustomerResponse;
import com.example.signeasy.web.dto.PlanDtos.PlanPriceResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;
import java.time.LocalDate;

public class SubscriptionDtos {
    public record SubscriptionResponse(KeyResponse key, CustomerResponse customer,
                                       PlanPriceResponse planPrice, SubscriptionStatus status,
                                       LocalDate startDate, LocalDate trialEndDate,
                                       LocalDate nextBillingDate, LocalDate endDate) {}

    public record SubscribeRequest(@NotNull UUID customerId, @NotBlank String planType, @NotNull Period period) {
    }

    public record ChangePlanRequest(@NotBlank String newPlanType, @NotNull Period newPeriod) {
    }
}
