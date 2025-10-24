package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.CustomerRepositoryPort;
import com.example.signeasy.application.ports.PlanRepositoryPort;
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
    private PlanRepositoryPort planRepositoryPort;
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
        Plan plan = buildPlan(UUID.randomUUID(), PlanType.PRO, Period.MONTHLY, 14);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(planRepositoryPort.findByPlanTypeAndTenantId(plan.getPlanType().name(), tenantId)).thenReturn(Optional.of(plan));
        when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0, Subscription.class));

        LocalDate today = LocalDate.now();

        Subscription subscription = subscriptionService.subscribe(customerId, plan.getPlanType().name());

        assertEquals(today, subscription.getStartDate());
        assertEquals(today.plusDays(plan.getTrialDays()), subscription.getTrialEndDate());
        assertEquals(subscription.getTrialEndDate(), subscription.getNextBillingDate());
        assertEquals(Subscription.Status.IN_TRIAL, subscription.getStatus());
        assertEquals(customer, subscription.getCustomer());
        assertEquals(plan, subscription.getPlan());

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepositoryPort).save(captor.capture());
        assertSame(subscription, captor.getValue());
    }

    @Test
    void subscribeActivatesImmediatelyWhenPlanHasNoTrial() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId);
        Plan plan = buildPlan(UUID.randomUUID(), PlanType.BASIC, Period.YEARLY, 0);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(planRepositoryPort.findByPlanTypeAndTenantId(plan.getPlanType().name(), tenantId)).thenReturn(Optional.of(plan));
        when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0, Subscription.class));

        LocalDate today = LocalDate.now();

        Subscription subscription = subscriptionService.subscribe(customerId, plan.getPlanType().name());

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

        assertThrows(BusinessException.class, () -> subscriptionService.subscribe(customerId, PlanType.BASIC.name()));
        verify(subscriptionRepositoryPort, never()).save(any());
    }

    @Test
    void subscribeThrowsWhenPlanDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.PRO.name(), tenantId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> subscriptionService.subscribe(customerId, PlanType.PRO.name()));
        verify(subscriptionRepositoryPort, never()).save(any());
    }

    @Test
    void changePlanUpdatesSubscriptionDetails() {
        UUID subscriptionId = UUID.randomUUID();
        Subscription existingSubscription = new Subscription();
        existingSubscription.setKey(new SubscriptionKey(subscriptionId, UUID.randomUUID(), UUID.randomUUID(), tenantId));
        existingSubscription.setPlan(buildPlan(UUID.randomUUID(), PlanType.BASIC, Period.MONTHLY, 0));

        Plan newPlan = buildPlan(UUID.randomUUID(), PlanType.PRO, Period.MONTHLY, 0);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(subscriptionRepositoryPort.findByIdAndTenantId(subscriptionId, tenantId)).thenReturn(Optional.of(existingSubscription));
        when(planRepositoryPort.findByPlanTypeAndTenantId(newPlan.getPlanType().name(), tenantId)).thenReturn(Optional.of(newPlan));
        when(subscriptionRepositoryPort.save(existingSubscription)).thenReturn(existingSubscription);

        LocalDate today = LocalDate.now();

        Subscription updated = subscriptionService.changePlan(subscriptionId, newPlan.getPlanType().name());

        assertEquals(newPlan, updated.getPlan());
        assertEquals(today.plusMonths(1), updated.getNextBillingDate());
        verify(subscriptionRepositoryPort).save(existingSubscription);
    }

    @Test
    void changePlanThrowsWhenSubscriptionMissing() {
        UUID subscriptionId = UUID.randomUUID();
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(subscriptionRepositoryPort.findByIdAndTenantId(subscriptionId, tenantId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> subscriptionService.changePlan(subscriptionId, PlanType.BASIC.name()));
        verify(subscriptionRepositoryPort, never()).save(any());
    }

    @Test
    void cancelMarksSubscriptionAsCanceled() {
        UUID subscriptionId = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setStatus(Subscription.Status.ACTIVE);
        subscription.setKey(new SubscriptionKey(subscriptionId, UUID.randomUUID(), UUID.randomUUID(), tenantId));

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(subscriptionRepositoryPort.findByIdAndTenantId(subscriptionId, tenantId)).thenReturn(Optional.of(subscription));
        when(subscriptionRepositoryPort.save(subscription)).thenAnswer(invocation -> invocation.getArgument(0, Subscription.class));

        LocalDate today = LocalDate.now();

        Subscription canceled = subscriptionService.cancel(subscriptionId);

        assertEquals(Subscription.Status.CANCELED, canceled.getStatus());
        assertEquals(today, canceled.getEndDate());
        verify(subscriptionRepositoryPort).save(subscription);
    }

    private Customer buildCustomer(UUID customerId) {
        CustomerKey key = new CustomerKey(customerId, tenantId);
        Customer customer = new Customer();
        customer.setKey(key);
        customer.setEmail("user@example.com");
        customer.setName("User");
        return customer;
    }

    private Plan buildPlan(UUID planId, PlanType planType, Period period, int trialDays) {
        PlanKey key = new PlanKey(planId, tenantId);
        Plan plan = new Plan();
        plan.setKey(key);
        plan.setPlanType(planType);
        plan.setName(planType.name());
        plan.setPeriod(period);
        plan.setTrialDays(trialDays);
        plan.setPriceCents(1999);
        plan.setActive(true);
        return plan;
    }
}
