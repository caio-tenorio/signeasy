package com.example.signeasy.application.ports.inbound;

import com.example.signeasy.domain.model.plan.Plan;

import java.util.List;

public interface PlanInboundPort {
    Plan create(Plan plan);
    List<Plan> listActive();
}
