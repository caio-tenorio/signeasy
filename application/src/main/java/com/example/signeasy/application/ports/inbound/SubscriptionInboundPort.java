package com.example.signeasy.application.ports.inbound;

import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.Subscription;

import java.util.UUID;

public interface SubscriptionInboundPort {
    Subscription subscribe(UUID customerId, String planType, Period period);
    Subscription changePlan(UUID subscriptionId, String planType, Period period);
    Subscription cancel(UUID subscriptionId);
}
