package com.example.subscriptions.web.dto;

import jakarta.validation.constraints.*;

public class PlanDtos {
    public record CreatePlanRequest(@NotBlank String code,
                                    @NotBlank String name,
                                    @Min(0) long priceCents,
                                    @NotNull Period period,
                                    @Min(0) int trialDays) {
        public enum Period {MONTHLY, YEARLY}
    }
}