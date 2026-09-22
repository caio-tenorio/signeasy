package com.example.signeasy.application.ports.inbound;

import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.PlanPrice;

import java.util.List;

public interface PlanPriceInboundPort {
    PlanPrice create(String planType, Period period, long priceCents);
    List<PlanPrice> listActive();
}
