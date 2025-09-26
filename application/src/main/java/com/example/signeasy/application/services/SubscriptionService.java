package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.CustomerRepositoryPort;
import com.example.signeasy.application.ports.PlanRepositoryPort;
import com.example.signeasy.application.ports.SubscriptionRepositoryPort;
import com.example.signeasy.application.ports.TenantContext;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.domain.model.Subscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class SubscriptionService {
    private final CustomerRepositoryPort customerRepositoryPort;
    private final PlanRepositoryPort planRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final TenantContext tenantContext;

    public SubscriptionService(CustomerRepositoryPort customerRepositoryPort,
                               PlanRepositoryPort planRepositoryPort,
                               SubscriptionRepositoryPort subscriptionRepositoryPort,
                               TenantContext tenantContext) {
        this.customerRepositoryPort = customerRepositoryPort;
        this.planRepositoryPort = planRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.tenantContext = tenantContext;
    }

    public Subscription subscribe(UUID customerId, String planType) {
        final String tenantId = tenantContext.currentTenantId();

        var customer = customerRepositoryPort.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new BusinessException("Customer not found"));
        var plan = planRepositoryPort.findByPlanTypeAndTenantId(planType, tenantId).orElseThrow(() -> new BusinessException("Plan not found"));

        var subscription = new Subscription();
        subscription.setCustomer(customer);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDate.now());
        if (plan.getTrialDays() > 0) {
            subscription.setStatus(Subscription.Status.IN_TRIAL);
            subscription.setTrialEndDate(LocalDate.now().plusDays(plan.getTrialDays()));
            subscription.setNextBillingDate(subscription.getTrialEndDate());
        } else {
            subscription.setStatus(Subscription.Status.ACTIVE);
            subscription.setNextBillingDate(nextBillingFrom(plan));
        }
        return subscriptionRepositoryPort.save(subscription);
    }

    public Subscription changePlan(UUID subscriptionId, String planType) {
        final String tenantId = tenantContext.currentTenantId();

        var subscription = subscriptionRepositoryPort.findByIdAndTenantId(subscriptionId, tenantId).orElseThrow(() -> new BusinessException("Subscription not found"));
        var plan = planRepositoryPort.findByPlanTypeAndTenantId(planType, tenantId).orElseThrow(() -> new BusinessException("Plan not found"));
        subscription.setPlan(plan);
        subscription.setNextBillingDate(nextBillingFrom(plan));
        return subscriptionRepositoryPort.save(subscription);
    }

    public Subscription cancel(UUID subscriptionId) {
        var s = subscriptionRepositoryPort.findByIdAndTenantId(subscriptionId, tenantContext.currentTenantId()).orElseThrow(() -> new BusinessException("Subscription not found"));
        s.setStatus(Subscription.Status.CANCELED);
        s.setEndDate(LocalDate.now());
        return subscriptionRepositoryPort.save(s);
    }

    private LocalDate nextBillingFrom(Plan plan) {
        return LocalDate.now().plus(plan.getPeriod() == Period.MONTHLY ? java.time.Period.ofMonths(1)
                : java.time.Period.ofYears(1));
    }
}