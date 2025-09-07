package com.example.signeasy.domain.model;

import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.plan.Plan;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    @EmbeddedId
    private SubscriptionKey key = new SubscriptionKey();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "customer_id", referencedColumnName = "id", insertable = false, updatable = false),
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false)
    })
    private Customer customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "plan_id", referencedColumnName = "id", insertable = false, updatable = false),
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false)
    })
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.IN_TRIAL;

    private LocalDate startDate;
    private LocalDate trialEndDate;
    private LocalDate nextBillingDate;
    private LocalDate endDate;

    public enum Status {IN_TRIAL, ACTIVE, SUSPENDED, CANCELED, EXPIRED}

    public Subscription() {}

    public Subscription(SubscriptionKey key, Customer customer, Plan plan, Status status, LocalDate startDate, LocalDate trialEndDate, LocalDate nextBillingDate, LocalDate endDate) {
        this.key = key;
        this.customer = customer;
        this.plan = plan;
        this.status = status;
        this.startDate = startDate;
        this.trialEndDate = trialEndDate;
        this.nextBillingDate = nextBillingDate;
        this.endDate = endDate;
    }

    public SubscriptionKey getKey() {
        return key;
    }

    public void setKey(SubscriptionKey key) {
        this.key = key;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null && key != null) {
            key.setCustomerId(customer.getKey().getId());
            key.setTenantId(customer.getKey().getTenantId());
        }
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
        if (plan != null && key != null) {
            key.setPlanId(plan.getKey().getId());
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getTrialEndDate() {
        return trialEndDate;
    }

    public void setTrialEndDate(LocalDate trialEndDate) {
        this.trialEndDate = trialEndDate;
    }

    public LocalDate getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}