package com.example.subscriptions.web.dto;

import com.example.subscriptions.domain.common.Period;
import com.example.subscriptions.domain.common.PlanType;
import jakarta.validation.constraints.*;

public class PlanDtos {
    public record CreatePlanRequest(@NotNull PlanType planType,
                                    @NotBlank String name,
                                    @Min(0) long priceCents,
                                    @NotNull Period period,
                                    @Min(0) int trialDays) {
    }
}