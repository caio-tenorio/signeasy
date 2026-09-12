package com.example.signeasy.domain.model;

import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.plan.PlanPrice;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    @EmbeddedId
    private SubscriptionKey key = new SubscriptionKey();

    // Scalar IDs own the foreign-key writes; associations share tenant_id read-only.
    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "plan_price_id", nullable = false)
    private UUID planPriceId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "customer_id", referencedColumnName = "id", insertable = false, updatable = false),
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false)
    })
    private Customer customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "plan_price_id", referencedColumnName = "id", insertable = false, updatable = false),
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false)
    })
    private PlanPrice planPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.IN_TRIAL;

    private LocalDate startDate;
    private LocalDate trialEndDate;
    private LocalDate nextBillingDate;
    private LocalDate endDate;

    public enum Status {IN_TRIAL, ACTIVE, SUSPENDED, CANCELED, EXPIRED}

    public Subscription() {}

    public Subscription(SubscriptionKey key, Customer customer, PlanPrice planPrice, Status status, LocalDate startDate, LocalDate trialEndDate, LocalDate nextBillingDate, LocalDate endDate) {
        this.key = key;
        setCustomer(customer);
        setPlanPrice(planPrice);
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
        requireTenant(customer == null || customer.getKey() == null ? null : customer.getKey().getTenantId());
        this.customer = customer;
        this.customerId = customer.getKey().getId();
    }

    public PlanPrice getPlanPrice() {
        return planPrice;
    }

    public void setPlanPrice(PlanPrice planPrice) {
        requireTenant(planPrice == null || planPrice.getKey() == null ? null : planPrice.getKey().getTenantId());
        this.planPrice = planPrice;
        this.planPriceId = planPrice.getKey().getId();
    }

    private void requireTenant(String tenantId) {
        if (key == null || tenantId == null || !Objects.equals(key.getTenantId(), tenantId)) {
            throw new BusinessException("Subscription, customer and plan price must belong to the same tenant");
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