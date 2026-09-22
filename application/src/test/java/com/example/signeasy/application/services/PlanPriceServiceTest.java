package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.outbound.PlanPriceRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.PlanRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.TenantContextOutboundPort;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.domain.model.plan.PlanKey;
import com.example.signeasy.domain.model.plan.PlanPrice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanPriceServiceTest {

    @Mock
    private PlanRepositoryOutboundPort planRepositoryPort;
    @Mock
    private PlanPriceRepositoryOutboundPort planPriceRepositoryPort;
    @Mock
    private TenantContextOutboundPort tenantContext;

    @InjectMocks
    private PlanPriceService planPriceService;

    private final String tenantId = "tenant-plan-price";

    @Test
    void createPersistsPriceForExistingPlanWhenPeriodIsUnique() {
        Plan plan = new Plan(new PlanKey(UUID.randomUUID(), tenantId), PlanType.BASIC, "Basic", 0, true);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.BASIC.name(), tenantId)).thenReturn(Optional.of(plan));
        when(planPriceRepositoryPort.findByPlanTypeAndPeriodAndTenantId(PlanType.BASIC.name(), Period.MONTHLY, tenantId))
                .thenReturn(Optional.empty());
        when(planPriceRepositoryPort.create(any(PlanPrice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanPrice saved = planPriceService.create(PlanType.BASIC.name(), Period.MONTHLY, 9900);

        assertNotNull(saved.getKey().getId());
        assertEquals(tenantId, saved.getKey().getTenantId());
        assertSame(plan, saved.getPlan());
        assertEquals(Period.MONTHLY, saved.getPeriod());
        assertEquals(9900, saved.getPriceCents());
        verify(planPriceRepositoryPort).create(saved);
    }

    @Test
    void createThrowsWhenPlanDoesNotExist() {
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.PRO.name(), tenantId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> planPriceService.create(PlanType.PRO.name(), Period.YEARLY, 1000));
        verify(planPriceRepositoryPort, never()).create(any());
    }

    @Test
    void createThrowsWhenPriceAlreadyExistsForPeriod() {
        Plan plan = new Plan(new PlanKey(UUID.randomUUID(), tenantId), PlanType.BASIC, "Basic", 0, true);
        PlanPrice existing = new PlanPrice();

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.BASIC.name(), tenantId)).thenReturn(Optional.of(plan));
        when(planPriceRepositoryPort.findByPlanTypeAndPeriodAndTenantId(PlanType.BASIC.name(), Period.MONTHLY, tenantId))
                .thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> planPriceService.create(PlanType.BASIC.name(), Period.MONTHLY, 9900));
        verify(planPriceRepositoryPort, never()).create(any());
    }

    @Test
    void createAllowsSamePlanTypeWithDifferentPeriods() {
        Plan plan = new Plan(new PlanKey(UUID.randomUUID(), tenantId), PlanType.BASIC, "Basic", 0, true);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.BASIC.name(), tenantId)).thenReturn(Optional.of(plan));
        when(planPriceRepositoryPort.findByPlanTypeAndPeriodAndTenantId(PlanType.BASIC.name(), Period.YEARLY, tenantId))
                .thenReturn(Optional.empty());
        when(planPriceRepositoryPort.create(any(PlanPrice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanPrice saved = planPriceService.create(PlanType.BASIC.name(), Period.YEARLY, 99000);

        assertEquals(Period.YEARLY, saved.getPeriod());
        verify(planPriceRepositoryPort).create(saved);
    }

    @Test
    void listActiveUsesTenantContext() {
        List<PlanPrice> expected = List.of(new PlanPrice(), new PlanPrice());
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planPriceRepositoryPort.listActivePricesByTenantId(tenantId)).thenReturn(expected);

        List<PlanPrice> result = planPriceService.listActive();

        assertSame(expected, result);
        verify(planPriceRepositoryPort).listActivePricesByTenantId(tenantId);
    }
}
