package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.PlanPriceRepositoryPort;
import com.example.signeasy.application.ports.PlanRepositoryPort;
import com.example.signeasy.application.ports.TenantContext;
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
public class PlanPriceService {
    private final PlanRepositoryPort planRepositoryPort;
    private final PlanPriceRepositoryPort planPriceRepositoryPort;
    private final TenantContext tenantContext;

    public PlanPriceService(PlanRepositoryPort planRepositoryPort, PlanPriceRepositoryPort planPriceRepositoryPort, TenantContext tenantContext) {
        this.planRepositoryPort = planRepositoryPort;
        this.planPriceRepositoryPort = planPriceRepositoryPort;
        this.tenantContext = tenantContext;
    }

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
        return planPriceRepositoryPort.save(price);
    }

    public List<PlanPrice> listActive() {
        return planPriceRepositoryPort.listActivePricesByTenantId(tenantContext.currentTenantId());
    }
}
