package com.example.subscriptions.web.controller;

import com.example.subscriptions.application.services.SubscriptionService;
import com.example.subscriptions.domain.model.Subscription;
import com.example.subscriptions.web.dto.SubscriptionDtos.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subs;

    public SubscriptionController(SubscriptionService subs) {
        this.subs = subs;
    }

    @PostMapping
    public Subscription subscribe(@RequestBody @Valid SubscribeRequest req) {
        return subs.subscribe(req.customerId(), req.planCode());
    }

    @PostMapping("/{id}/change-plan")
    public Subscription changePlan(@PathVariable UUID id, @RequestBody @Valid ChangePlanRequest req) {
        return subs.changePlan(id, req.newPlanCode());
    }

    @PostMapping("/{id}/cancel")
    public Subscription cancel(@PathVariable UUID id) {
        return subs.cancel(id);
    }
}