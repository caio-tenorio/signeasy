package com.example.signeasy.web.controller;

import com.example.signeasy.application.services.SubscriptionService;
import com.example.signeasy.domain.model.Subscription;
import com.example.signeasy.web.dto.SubscriptionDtos.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public Subscription subscribe(@RequestBody @Valid SubscribeRequest req) {
        return subscriptionService.subscribe(req.customerId(), req.planType());
    }

    @PostMapping("/{id}/change-plan")
    public Subscription changePlan(@PathVariable UUID id, @RequestBody @Valid ChangePlanRequest req) {
        return subscriptionService.changePlan(id, req.newPlanType());
    }

    @PostMapping("/{id}/cancel")
    public Subscription cancel(@PathVariable UUID id) {
        return subscriptionService.cancel(id);
    }
}