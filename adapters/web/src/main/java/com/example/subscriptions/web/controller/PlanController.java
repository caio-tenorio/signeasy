package com.example.subscriptions.web.controller;

import com.example.subscriptions.application.services.PlanService;
import com.example.subscriptions.domain.model.Plan;
import com.example.subscriptions.web.dto.PlanDtos.CreatePlanRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
    private final PlanService plans;

    public PlanController(PlanService plans) {
        this.plans = plans;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Plan create(@RequestBody @Validated CreatePlanRequest req) {
        var p = new Plan();
        p.setCode(req.code());
        p.setName(req.name());
        p.setPriceCents(req.priceCents());
        p.setPeriod(req.period() == CreatePlanRequest.Period.MONTHLY ? Plan.Period.MONTHLY : Plan.Period.YEARLY);
        p.setTrialDays(req.trialDays());
        return plans.create(p);
    }

    @GetMapping
    public List<Plan> list() {
        return plans.listActive();
    }
}