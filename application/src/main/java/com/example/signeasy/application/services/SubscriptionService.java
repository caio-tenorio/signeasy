package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.CustomerRepositoryPort;
import com.example.signeasy.application.ports.PlanRepositoryPort;
import com.example.signeasy.application.ports.SubscriptionRepositoryPort;
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
    private final CustomerRepositoryPort customers;
    private final PlanRepositoryPort plans;
    private final SubscriptionRepositoryPort subs;

    public SubscriptionService(CustomerRepositoryPort customers, PlanRepositoryPort plans, SubscriptionRepositoryPort subs) {
        this.customers = customers;
        this.plans = plans;
        this.subs = subs;
    }

    public Subscription subscribe(UUID customerId, String planType) {
        var customer = customers.findById(customerId).orElseThrow(() -> new BusinessException("Customer not found"));
        var plan = plans.findByPlanType(planType).orElseThrow(() -> new BusinessException("Plan not found"));

        var s = new Subscription();
        s.setCustomer(customer);
        s.setPlan(plan);
        s.setStartDate(LocalDate.now());
        if (plan.getTrialDays() > 0) {
            s.setStatus(Subscription.Status.IN_TRIAL);
            s.setTrialEndDate(LocalDate.now().plusDays(plan.getTrialDays()));
            s.setNextBillingDate(s.getTrialEndDate());
        } else {
            s.setStatus(Subscription.Status.ACTIVE);
            s.setNextBillingDate(nextBillingFrom(plan));
        }
        return subs.save(s);
    }

    public Subscription changePlan(UUID subscriptionId, String planType) {
        var s = subs.findById(subscriptionId).orElseThrow(() -> new BusinessException("Subscription not found"));
        var plan = plans.findByPlanType(planType).orElseThrow(() -> new BusinessException("Plan not found"));
        s.setPlan(plan);
        s.setNextBillingDate(nextBillingFrom(plan)); // simplificado
        return subs.save(s);
    }

    public Subscription cancel(UUID subscriptionId) {
        var s = subs.findById(subscriptionId).orElseThrow(() -> new BusinessException("Subscription not found"));
        s.setStatus(Subscription.Status.CANCELED);
        s.setEndDate(LocalDate.now());
        return subs.save(s);
    }

    private LocalDate nextBillingFrom(Plan plan) {
        return LocalDate.now().plus(plan.getPeriod() == Period.MONTHLY ? java.time.Period.ofMonths(1)
                : java.time.Period.ofYears(1));
    }
}