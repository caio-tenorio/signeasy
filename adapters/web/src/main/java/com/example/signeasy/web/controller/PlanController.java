package com.example.signeasy.web.controller;

import com.example.signeasy.application.services.PlanPriceService;
import com.example.signeasy.application.services.PlanService;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.web.dto.PlanDtos.PlanResponse;
import com.example.signeasy.web.dto.PlanDtos.PlanPriceResponse;
import com.example.signeasy.web.mapper.ApiMapper;
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
    public PlanResponse create(@RequestBody @Validated CreatePlanRequest req) {
        var p = new Plan();
        p.setPlanType(ApiMapper.toDomain(req.planType()));
        p.setName(req.name());
        p.setTrialDays(req.trialDays());
        return ApiMapper.toResponse(plans.create(p));
    }

    @GetMapping
    public List<PlanResponse> list() {
        return plans.listActive().stream().map(ApiMapper::toResponse).toList();
    }

    @PostMapping("/prices")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public PlanPriceResponse createPrice(@RequestBody @Validated CreatePlanPriceRequest req) {
        return ApiMapper.toResponse(prices.create(req.planType().name(), ApiMapper.toDomain(req.period()), req.priceCents()));
    }

    @GetMapping("/prices")
    public List<PlanPriceResponse> listPrices() {
        return prices.listActive().stream().map(ApiMapper::toResponse).toList();
    }
}
