package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.outbound.PlanRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.TenantContextOutboundPort;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.domain.model.plan.PlanKey;
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
class PlanServiceTest {

    @Mock
    private PlanRepositoryOutboundPort planRepositoryPort;
    @Mock
    private TenantContextOutboundPort tenantContext;

    @InjectMocks
    private PlanService planService;

    private final String tenantId = "tenant-plan";

    @Test
    void createWithoutKeyPersistsCompleteKey() {
        Plan input = new Plan();
        input.setPlanType(PlanType.BASIC);
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.BASIC.name(), tenantId)).thenReturn(Optional.empty());
        when(planRepositoryPort.create(input)).thenAnswer(invocation -> {
            Plan saved = invocation.getArgument(0);
            assertNotNull(saved.getKey().getId());
            assertEquals(4, saved.getKey().getId().version());
            assertEquals(tenantId, saved.getKey().getTenantId());
            return saved;
        });

        assertSame(input, planService.create(input));
        verify(planRepositoryPort).create(input);
    }

    @Test
    void createWithIncompleteKeyPersistsCompleteKey() {
        Plan input = new Plan();
        input.setPlanType(PlanType.BASIC);
        input.setKey(new PlanKey(null, "other-tenant"));
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.BASIC.name(), tenantId)).thenReturn(Optional.empty());
        when(planRepositoryPort.create(input)).thenAnswer(invocation -> {
            Plan saved = invocation.getArgument(0);
            assertNotNull(saved.getKey().getId());
            assertEquals(4, saved.getKey().getId().version());
            assertEquals(tenantId, saved.getKey().getTenantId());
            return saved;
        });

        assertSame(input, planService.create(input));
        verify(planRepositoryPort).create(input);
    }

    @Test
    void createWithExistingIdPersistsCompleteKey() {
        UUID existingId = UUID.randomUUID();
        Plan input = new Plan();
        input.setPlanType(PlanType.BASIC);
        input.setKey(new PlanKey(existingId, "other-tenant"));
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.BASIC.name(), tenantId)).thenReturn(Optional.empty());
        when(planRepositoryPort.create(input)).thenAnswer(invocation -> {
            Plan saved = invocation.getArgument(0);
            assertNotNull(saved.getKey().getId());
            assertEquals(existingId, saved.getKey().getId());
            assertEquals(tenantId, saved.getKey().getTenantId());
            return saved;
        });

        assertSame(input, planService.create(input));
        verify(planRepositoryPort).create(input);
    }

    @Test
    void createSetsTenantAndPersistsWhenUnique() {
        Plan plan = new Plan();
        plan.setPlanType(PlanType.BASIC);
        plan.setName("Basic Plan");
        plan.setTrialDays(7);

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(planRepositoryPort.findByPlanTypeAndTenantId(PlanType.BASIC.name(), tenantId)).thenReturn(Optional.empty());
        when(planRepositoryPort.create(plan)).thenReturn(plan);

        Plan saved = planService.create(plan);

        assertSame(plan, saved);
        assertNotNull(plan.getKey());
        assertEquals(tenantId, plan.getKey().getTenantId());
        verify(planRepositoryPort).create(plan);
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
        verify(planRepositoryPort, never()).create(any());
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
