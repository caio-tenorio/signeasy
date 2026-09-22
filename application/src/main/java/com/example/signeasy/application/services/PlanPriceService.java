package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.inbound.PlanPriceInboundPort;
import com.example.signeasy.application.ports.outbound.PlanPriceRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.PlanRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.TenantContextOutboundPort;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.domain.model.plan.PlanPriceKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlanPriceService implements PlanPriceInboundPort {
    private final PlanRepositoryOutboundPort planRepositoryPort;
    private final PlanPriceRepositoryOutboundPort planPriceRepositoryPort;
    private final TenantContextOutboundPort tenantContext;

    public PlanPriceService(PlanRepositoryOutboundPort planRepositoryPort, PlanPriceRepositoryOutboundPort planPriceRepositoryPort, TenantContextOutboundPort tenantContext) {
        this.planRepositoryPort = planRepositoryPort;
        this.planPriceRepositoryPort = planPriceRepositoryPort;
        this.tenantContext = tenantContext;
    }

    @Override
    public PlanPrice create(String planType, Period period, long priceCents) {
        final String tenantId = tenantContext.currentTenantId();

        var plan = planRepositoryPort.findByPlanTypeAndTenantId(planType, tenantId)
                .orElseThrow(() -> new BusinessException("Plan not found"));

        planPriceRepositoryPort.findByPlanTypeAndPeriodAndTenantId(planType, period, tenantId).ifPresent(x -> {
            throw new BusinessException("Plan price already exists for this period");
        });

        var price = new PlanPrice();
        price.setKey(new PlanPriceKey(UUID.randomUUID(), tenantId));
        price.setPlan(plan);
        price.setPeriod(period);
        price.setPriceCents(priceCents);
        return planPriceRepositoryPort.create(price);
    }

    @Override
    public List<PlanPrice> listActive() {
        return planPriceRepositoryPort.listActivePricesByTenantId(tenantContext.currentTenantId());
    }
}
