package com.example.signeasy.web.controller;

import com.example.signeasy.application.services.PlanPriceService;
import com.example.signeasy.application.services.PlanService;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.web.dto.PlanDtos.CreatePlanPriceRequest;
import com.example.signeasy.web.dto.PlanDtos.CreatePlanRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
    private final PlanService plans;
    private final PlanPriceService prices;

    public PlanController(PlanService plans, PlanPriceService prices) {
        this.plans = plans;
        this.prices = prices;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Plan create(@RequestBody @Validated CreatePlanRequest req) {
        var p = new Plan();
        p.setPlanType(req.planType());
        p.setName(req.name());
        p.setTrialDays(req.trialDays());
        return plans.create(p);
    }

    @GetMapping
    public List<Plan> list() {
        return plans.listActive();
    }

    @PostMapping("/prices")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public PlanPrice createPrice(@RequestBody @Validated CreatePlanPriceRequest req) {
        return prices.create(req.planType().name(), req.period(), req.priceCents());
    }

    @GetMapping("/prices")
    public List<PlanPrice> listPrices() {
        return prices.listActive();
    }
}