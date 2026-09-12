package com.example.signeasy.web.dto;

import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.common.PlanType;
import jakarta.validation.constraints.*;

public class PlanDtos {
    public record CreatePlanRequest(@NotNull PlanType planType,
                                    @NotBlank String name,
                                    @Min(0) int trialDays) {
    }

    public record CreatePlanPriceRequest(@NotNull PlanType planType,
                                         @NotNull Period period,
                                         @Min(0) long priceCents) {
    }
}