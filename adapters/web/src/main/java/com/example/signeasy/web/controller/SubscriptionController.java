package com.example.signeasy.web.controller;

import com.example.signeasy.application.services.SubscriptionService;
import com.example.signeasy.web.mapper.ApiMapper;
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
    public SubscriptionResponse subscribe(@RequestBody @Valid SubscribeRequest req) {
        return ApiMapper.toResponse(subscriptionService.subscribe(req.customerId(), req.planType(), ApiMapper.toDomain(req.period())));
    }

    @PostMapping("/{id}/change-plan")
    public SubscriptionResponse changePlan(@PathVariable("id") UUID id, @RequestBody @Valid ChangePlanRequest req) {
        return ApiMapper.toResponse(subscriptionService.changePlan(id, req.newPlanType(), ApiMapper.toDomain(req.newPeriod())));
    }

    @PostMapping("/{id}/cancel")
    public SubscriptionResponse cancel(@PathVariable("id") UUID id) {
        return ApiMapper.toResponse(subscriptionService.cancel(id));
    }
}
