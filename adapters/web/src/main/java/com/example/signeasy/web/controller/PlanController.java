package com.example.signeasy.web.controller;

import com.example.signeasy.application.services.PlanService;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.web.dto.PlanDtos.CreatePlanRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
    private PlanService plans;

    public PlanController(PlanService plans) {
        this.plans = plans;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Plan create(@RequestBody @Validated CreatePlanRequest req) {
        var p = new Plan();
        p.setPlanType(req.planType());
        p.setName(req.name());
        p.setPriceCents(req.priceCents());
        p.setPeriod(req.period() == Period.MONTHLY ? Period.MONTHLY : Period.YEARLY);
        p.setTrialDays(req.trialDays());
        return plans.create(p);
    }

    @GetMapping
    public List<Plan> list() {
        return plans.listActive();
    }
}