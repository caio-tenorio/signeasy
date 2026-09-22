package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.inbound.SubscriptionInboundPort;
import com.example.signeasy.application.ports.outbound.CustomerRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.PlanPriceRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.SubscriptionRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.TenantContextOutboundPort;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.domain.model.Subscription;
import com.example.signeasy.domain.model.SubscriptionKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class SubscriptionService implements SubscriptionInboundPort {
    private final CustomerRepositoryOutboundPort customerRepositoryPort;
    private final PlanPriceRepositoryOutboundPort planPriceRepositoryPort;
    private final SubscriptionRepositoryOutboundPort subscriptionRepositoryPort;
    private final TenantContextOutboundPort tenantContext;

    public SubscriptionService(CustomerRepositoryOutboundPort customerRepositoryPort,
                               PlanPriceRepositoryOutboundPort planPriceRepositoryPort,
                               SubscriptionRepositoryOutboundPort subscriptionRepositoryPort,
                               TenantContextOutboundPort tenantContext) {
        this.customerRepositoryPort = customerRepositoryPort;
        this.planPriceRepositoryPort = planPriceRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.tenantContext = tenantContext;
    }

    @Override
    public Subscription subscribe(UUID customerId, String planType, Period period) {
        final String tenantId = tenantContext.currentTenantId();

        var customer = customerRepositoryPort.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new BusinessException("Customer not found"));
        var planPrice = planPriceRepositoryPort.findByPlanTypeAndPeriodAndTenantId(planType, period, tenantId)
                .orElseThrow(() -> new BusinessException("Plan not found"));

        var subscription = new Subscription();
        subscription.setKey(new SubscriptionKey(UUID.randomUUID(), tenantId));
        subscription.setCustomer(customer);
        subscription.setPlanPrice(planPrice);
        subscription.setStartDate(LocalDate.now());
        int trialDays = planPrice.getPlan().getTrialDays();
        if (trialDays > 0) {
            subscription.setStatus(Subscription.Status.IN_TRIAL);
            subscription.setTrialEndDate(LocalDate.now().plusDays(trialDays));
            subscription.setNextBillingDate(subscription.getTrialEndDate());
        } else {
            subscription.setStatus(Subscription.Status.ACTIVE);
            subscription.setNextBillingDate(nextBillingFrom(planPrice));
        }
        return subscriptionRepositoryPort.create(subscription);
    }

    @Override
    public Subscription changePlan(UUID subscriptionId, String planType, Period period) {
        final String tenantId = tenantContext.currentTenantId();

        var subscription = subscriptionRepositoryPort.findByIdAndTenantId(subscriptionId, tenantId).orElseThrow(() -> new BusinessException("Subscription not found"));
        var planPrice = planPriceRepositoryPort.findByPlanTypeAndPeriodAndTenantId(planType, period, tenantId)
                .orElseThrow(() -> new BusinessException("Plan not found"));
        subscription.setPlanPrice(planPrice);
        subscription.setNextBillingDate(nextBillingFrom(planPrice));
        return subscriptionRepositoryPort.update(subscription);
    }

    @Override
    public Subscription cancel(UUID subscriptionId) {
        var s = subscriptionRepositoryPort.findByIdAndTenantId(subscriptionId, tenantContext.currentTenantId()).orElseThrow(() -> new BusinessException("Subscription not found"));
        s.setStatus(Subscription.Status.CANCELED);
        s.setEndDate(LocalDate.now());
        return subscriptionRepositoryPort.update(s);
    }

    private LocalDate nextBillingFrom(PlanPrice planPrice) {
        return LocalDate.now().plus(planPrice.getPeriod() == Period.MONTHLY ? java.time.Period.ofMonths(1)
                : java.time.Period.ofYears(1));
    }
}