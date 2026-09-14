package com.example.signeasy.web.dto;

import com.example.signeasy.web.dto.ApiTypes.Period;
import com.example.signeasy.web.dto.ApiTypes.PlanType;
import jakarta.validation.constraints.*;

public class PlanDtos {
    public record PlanResponse(KeyResponse key, PlanType planType, String name,
                               int trialDays, boolean active) {}

    public record PlanPriceResponse(KeyResponse key, PlanResponse plan, Period period,
                                    long priceCents, boolean active) {}

    public record CreatePlanRequest(@NotNull PlanType planType,
                                    @NotBlank String name,
                                    @Min(0) int trialDays) {
    }

    public record CreatePlanPriceRequest(@NotNull PlanType planType,
                                         @NotNull Period period,
                                         @Min(0) long priceCents) {
    }
}
