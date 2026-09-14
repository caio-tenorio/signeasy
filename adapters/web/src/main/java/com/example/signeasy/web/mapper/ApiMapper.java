package com.example.signeasy.web.mapper;

import com.example.signeasy.domain.model.Subscription;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.web.dto.ApiTypes;
import com.example.signeasy.web.dto.KeyResponse;
import com.example.signeasy.web.dto.CustomerDtos.CustomerResponse;
import com.example.signeasy.web.dto.PlanDtos.PlanResponse;
import com.example.signeasy.web.dto.PlanDtos.PlanPriceResponse;
import com.example.signeasy.web.dto.SubscriptionDtos.SubscriptionResponse;

/** Explicit boundary between application objects and the HTTP representation. */
public final class ApiMapper {
    private ApiMapper() {}

    public static com.example.signeasy.domain.common.PlanType toDomain(ApiTypes.PlanType type) {
        return type == null ? null : com.example.signeasy.domain.common.PlanType.valueOf(type.name());
    }

    public static com.example.signeasy.domain.common.Period toDomain(ApiTypes.Period period) {
        return period == null ? null : com.example.signeasy.domain.common.Period.valueOf(period.name());
    }

    public static PlanResponse toResponse(Plan plan) {
        if (plan == null) return null;
        var key = plan.getKey();
        return new PlanResponse(key == null ? null : new KeyResponse(key.getId(), key.getTenantId()),
                plan.getPlanType() == null ? null : ApiTypes.PlanType.valueOf(plan.getPlanType().name()),
                plan.getName(), plan.getTrialDays(), plan.isActive());
    }

    public static PlanPriceResponse toResponse(PlanPrice price) {
        if (price == null) return null;
        var key = price.getKey();
        return new PlanPriceResponse(key == null ? null : new KeyResponse(key.getId(), key.getTenantId()),
                toResponse(price.getPlan()),
                price.getPeriod() == null ? null : ApiTypes.Period.valueOf(price.getPeriod().name()),
                price.getPriceCents(), price.isActive());
    }

    public static CustomerResponse toResponse(Customer customer) {
        if (customer == null) return null;
        var key = customer.getKey();
        return new CustomerResponse(key == null ? null : new KeyResponse(key.getId(), key.getTenantId()),
                customer.getName(), customer.getEmail(),
                customer.getStatus() == null ? null : ApiTypes.CustomerStatus.valueOf(customer.getStatus().name()));
    }

    public static SubscriptionResponse toResponse(Subscription subscription) {
        if (subscription == null) return null;
        var key = subscription.getKey();
        return new SubscriptionResponse(key == null ? null : new KeyResponse(key.getId(), key.getTenantId()),
                toResponse(subscription.getCustomer()), toResponse(subscription.getPlanPrice()),
                subscription.getStatus() == null ? null : ApiTypes.SubscriptionStatus.valueOf(subscription.getStatus().name()),
                subscription.getStartDate(), subscription.getTrialEndDate(),
                subscription.getNextBillingDate(), subscription.getEndDate());
    }
}
