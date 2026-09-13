package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.CustomerRepositoryPort;
import com.example.signeasy.application.ports.PlanPriceRepositoryPort;
import com.example.signeasy.application.ports.SubscriptionRepositoryPort;
import com.example.signeasy.application.ports.TenantContext;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.model.Subscription;
import com.example.signeasy.domain.model.SubscriptionKey;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.customer.CustomerKey;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.domain.model.plan.PlanKey;
import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.domain.model.plan.PlanPriceKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;
    @Mock
    private PlanPriceRepositoryPort planPriceRepositoryPort;
    @Mock
    private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock
    private TenantContext tenantContext;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private final String tenantId = "tenant-123";

    @Test
    void subscribeCreatesTrialSubscriptionWhenPlanHasTrialDays() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId);
        PlanPrice planPrice = buildPlanPrice(UUID.randomUUID(), PlanType.PRO, Period.MONTHLY, 14);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(planPriceRepositoryPort.findByPlanTypeAndPeriodAndTenantId(PlanType.PRO.name(), Period.MONTHLY, tenantId)).thenReturn(Optional.of(planPrice));
        when(subscriptionRepositoryPort.create(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0, Subscription.class));

        LocalDate today = LocalDate.now();

        Subscription subscription = subscriptionService.subscribe(customerId, PlanType.PRO.name(), Period.MONTHLY);

        assertNotNull(subscription.getKey().getId());
        assertEquals(tenantId, subscription.getKey().getTenantId());
        assertEquals(today, subscription.getStartDate());
        assertEquals(today.plusDays(planPrice.getPlan().getTrialDays()), subscription.getTrialEndDate());
        assertEquals(subscription.getTrialEndDate(), subscription.getNextBillingDate());
        assertEquals(Subscription.Status.IN_TRIAL, subscription.getStatus());
        assertEquals(customer, subscription.getCustomer());
        assertEquals(planPrice, subscription.getPlanPrice());

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepositoryPort).create(captor.capture());
        assertSame(subscription, captor.getValue());
    }

    @Test
    void subscribeActivatesImmediatelyWhenPlanHasNoTrial() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId);
        PlanPrice planPrice = buildPlanPrice(UUID.randomUUID(), PlanType.BASIC, Period.YEARLY, 0);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(planPriceRepositoryPort.findByPlanTypeAndPeriodAndTenantId(PlanType.BASIC.name(), Period.YEARLY, tenantId)).thenReturn(Optional.of(planPrice));
        when(subscriptionRepositoryPort.create(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0, Subscription.class));

        LocalDate today = LocalDate.now();

        Subscription subscription = subscriptionService.subscribe(customerId, PlanType.BASIC.name(), Period.YEARLY);

        assertNotNull(subscription.getKey().getId());
        assertEquals(tenantId, subscription.getKey().getTenantId());
        assertEquals(today, subscription.getStartDate());
        assertNull(subscription.getTrialEndDate());
        assertEquals(today.plusYears(1), subscription.getNextBillingDate());
        assertEquals(Subscription.Status.ACTIVE, subscription.getStatus());
    }

    @Test
    void subscribeThrowsWhenCustomerDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> subscriptionService.subscribe(customerId, PlanType.BASIC.name(), Period.MONTHLY));
        verify(subscriptionRepositoryPort, never()).create(any());
    }

    @Test
    void subscribeThrowsWhenPlanDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(planPriceRepositoryPort.findByPlanTypeAndPeriodAndTenantId(PlanType.PRO.name(), Period.MONTHLY, tenantId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> subscriptionService.subscribe(customerId, PlanType.PRO.name(), Period.MONTHLY));
        verify(subscriptionRepositoryPort, never()).create(any());
    }

    @Test
    void changePlanUpdatesSubscriptionDetails() {
        UUID subscriptionId = UUID.randomUUID();
        Subscription existingSubscription = new Subscription();
        existingSubscription.setKey(new SubscriptionKey(subscriptionId, tenantId));
        existingSubscription.setPlanPrice(buildPlanPrice(UUID.randomUUID(), PlanType.BASIC, Period.MONTHLY, 0));

        PlanPrice newPlanPrice = buildPlanPrice(UUID.randomUUID(), PlanType.PRO, Period.MONTHLY, 0);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(subscriptionRepositoryPort.findByIdAndTenantId(subscriptionId, tenantId)).thenReturn(Optional.of(existingSubscription));
        when(planPriceRepositoryPort.findByPlanTypeAndPeriodAndTenantId(PlanType.PRO.name(), Period.MONTHLY, tenantId)).thenReturn(Optional.of(newPlanPrice));
        when(subscriptionRepositoryPort.update(existingSubscription)).thenReturn(existingSubscription);

        LocalDate today = LocalDate.now();

        Subscription updated = subscriptionService.changePlan(subscriptionId, PlanType.PRO.name(), Period.MONTHLY);

        assertEquals(new SubscriptionKey(subscriptionId, tenantId), updated.getKey());
        assertEquals(newPlanPrice, updated.getPlanPrice());
        assertEquals(today.plusMonths(1), updated.getNextBillingDate());
        verify(subscriptionRepositoryPort).update(existingSubscription);
    }

    @Test
    void changePlanThrowsWhenSubscriptionMissing() {
        UUID subscriptionId = UUID.randomUUID();
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(subscriptionRepositoryPort.findByIdAndTenantId(subscriptionId, tenantId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> subscriptionService.changePlan(subscriptionId, PlanType.BASIC.name(), Period.MONTHLY));
        verify(subscriptionRepositoryPort, never()).update(any());
    }

    @Test
    void cancelMarksSubscriptionAsCanceled() {
        UUID subscriptionId = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setStatus(Subscription.Status.ACTIVE);
        subscription.setKey(new SubscriptionKey(subscriptionId, tenantId));

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(subscriptionRepositoryPort.findByIdAndTenantId(subscriptionId, tenantId)).thenReturn(Optional.of(subscription));
        when(subscriptionRepositoryPort.update(subscription)).thenAnswer(invocation -> invocation.getArgument(0, Subscription.class));

        LocalDate today = LocalDate.now();

        Subscription canceled = subscriptionService.cancel(subscriptionId);

        assertEquals(new SubscriptionKey(subscriptionId, tenantId), canceled.getKey());
        assertEquals(Subscription.Status.CANCELED, canceled.getStatus());
        assertEquals(today, canceled.getEndDate());
        verify(subscriptionRepositoryPort).update(subscription);
    }

    @Test
    void identityUsesOnlyIdAndTenantAndRejectsCrossTenantRelations() {
        UUID id = UUID.randomUUID();
        SubscriptionKey key = new SubscriptionKey(id, tenantId);
        assertEquals(key, new SubscriptionKey(id, tenantId));
        assertEquals(key.hashCode(), new SubscriptionKey(id, tenantId).hashCode());
        assertNotEquals(key, new SubscriptionKey(id, "other"));
        Subscription subscription = new Subscription();
        subscription.setKey(key);
        Customer customer = buildCustomer(UUID.randomUUID());
        customer.getKey().setTenantId("other");
        assertThrows(BusinessException.class, () -> subscription.setCustomer(customer));
        PlanPrice planPrice = buildPlanPrice(UUID.randomUUID(), PlanType.PRO, Period.MONTHLY, 0);
        planPrice.getKey().setTenantId("other");
        assertThrows(BusinessException.class, () -> subscription.setPlanPrice(planPrice));
        assertEquals(new SubscriptionKey(id, tenantId), subscription.getKey());
    }

    private Customer buildCustomer(UUID customerId) {
        CustomerKey key = new CustomerKey(customerId, tenantId);
        Customer customer = new Customer();
        customer.setKey(key);
        customer.setEmail("user@example.com");
        customer.setName("User");
        return customer;
    }

    private PlanPrice buildPlanPrice(UUID planId, PlanType planType, Period period, int trialDays) {
        Plan plan = new Plan();
        plan.setKey(new PlanKey(planId, tenantId));
        plan.setPlanType(planType);
        plan.setName(planType.name());
        plan.setTrialDays(trialDays);
        plan.setActive(true);

        PlanPrice planPrice = new PlanPrice();
        planPrice.setKey(new PlanPriceKey(UUID.randomUUID(), tenantId));
        planPrice.setPlan(plan);
        planPrice.setPeriod(period);
        planPrice.setPriceCents(1999);
        planPrice.setActive(true);
        return planPrice;
    }
}
