package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.PlanRepositoryPort;
import com.example.signeasy.application.ports.TenantContext;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.model.plan.Plan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepositoryPort planRepositoryPort;
    @Mock
    private TenantContext tenantContext;

    @InjectMocks
    private PlanService planService;

    private final String tenantId = "tenant-plan";

    @Test
    void createSetsTenantAndPersistsWhenUnique() {
        Plan plan = new Plan();
        plan.setPlanType(PlanType.BASIC);
        plan.setName("Basic Plan");
        plan.setPriceCents(9900);
        plan.setPeriod(Period.MONTHLY);
        plan.setTrialDays(7);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.BASIC.name(), tenantId)).thenReturn(Optional.empty());
        when(planRepositoryPort.save(plan)).thenReturn(plan);

        Plan saved = planService.create(plan);

        assertSame(plan, saved);
        assertNotNull(plan.getKey());
        assertEquals(tenantId, plan.getKey().getTenantId());
        verify(planRepositoryPort).save(plan);
    }

    @Test
    void createThrowsWhenPlanTypeAlreadyExists() {
        Plan plan = new Plan();
        plan.setPlanType(PlanType.PRO);
        plan.setName("Pro Plan");

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.PRO.name(), tenantId))
                .thenReturn(Optional.of(plan));

        assertThrows(BusinessException.class, () -> planService.create(plan));
        verify(planRepositoryPort, never()).save(any());
    }

    @Test
    void listActiveUsesTenantContext() {
        List<Plan> expected = List.of(new Plan(), new Plan());
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.listActivePlansByTenantId(tenantId)).thenReturn(expected);

        List<Plan> result = planService.listActive();

        assertSame(expected, result);
        verify(planRepositoryPort).listActivePlansByTenantId(tenantId);
    }
}
