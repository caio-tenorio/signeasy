package com.example.signeasy.persistence;

import com.example.signeasy.persistence.entity.*;

import com.example.signeasy.application.ports.outbound.CustomerRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.PlanRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.PlanPriceRepositoryOutboundPort;
import com.example.signeasy.application.services.CustomerService;
import com.example.signeasy.application.services.PlanService;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.customer.CustomerKey;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.domain.model.plan.PlanKey;
import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.domain.model.plan.PlanPriceKey;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CreationPersistenceIT.Config.class, properties = {
        "spring.jpa.hibernate.ddl-auto=validate", "spring.jpa.open-in-view=false"})
@Testcontainers
class CreationPersistenceIT {
    private static final String TENANT = "creation-test";

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan("com.example.signeasy.persistence.entity")
    @EnableJpaRepositories("com.example.signeasy.persistence.jpa")
    @ComponentScan({"com.example.signeasy.persistence.adapter", "com.example.signeasy.persistence.mapper"})
    static class Config {
        @Bean
        PlanService plans(PlanRepositoryOutboundPort repository) {
            return new PlanService(repository, () -> TENANT);
        }

        @Bean
        CustomerService customers(CustomerRepositoryOutboundPort repository) {
            return new CustomerService(repository, () -> TENANT);
        }
    }

    @Autowired PlanService plans;
    @Autowired CustomerService customers;
    @Autowired CustomerRepositoryOutboundPort customerRepository;
    @Autowired PlanRepositoryOutboundPort planRepository;
    @Autowired PlanPriceRepositoryOutboundPort priceRepository;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void createPlanWithoutKeyCommitsGeneratedIdAndTenant() {
        Plan input = new Plan();
        input.setPlanType(PlanType.BASIC);
        input.setName("Plano Teste");
        input.setTrialDays(1);

        // No surrounding test transaction: create must commit successfully.
        Plan saved = plans.create(input);
        UUID id = saved.getKey().getId();
        assertNotNull(id);
        assertEquals(4, id.version());
        assertEquals(TENANT, saved.getKey().getTenantId());

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            PlanJpaEntity reloaded = em.find(PlanJpaEntity.class, new PlanJpaKey(id, TENANT));
            assertNotNull(reloaded);
            assertEquals("Plano Teste", reloaded.getName());
            assertEquals(1, reloaded.getTrialDays());
            assertNull(em.find(PlanJpaEntity.class, new PlanJpaKey(id, "other-tenant")));
        });
    }

    @Test
    void createCustomersWithoutKeysCommitsDistinctIdsAndTenant() {
        Customer first = customers.create(customer("first@example.test"));
        Customer second = customers.create(customer("second@example.test"));
        assertNotNull(first.getKey().getId());
        assertNotNull(second.getKey().getId());
        assertNotEquals(first.getKey().getId(), second.getKey().getId());

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            for (Customer saved : new Customer[]{first, second}) {
                assertEquals(TENANT, saved.getKey().getTenantId());
                CustomerJpaEntity reloaded = em.find(CustomerJpaEntity.class, new CustomerJpaKey(saved.getKey().getId(), TENANT));
                assertNotNull(reloaded);
                assertEquals(saved.getEmail(), reloaded.getEmail());
                assertNotNull(reloaded.getCreatedAt());
                assertNull(em.find(CustomerJpaEntity.class, new CustomerJpaKey(saved.getKey().getId(), "other-tenant")));
            }
        });
    }

    @Test
    void provisionCustomerCommitsIdentityFromLogin() {
        UUID userId = UUID.randomUUID();
        customers.provisionIfNotExists(userId.toString(), TENANT, "login@example.test", "Login User");

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            CustomerJpaEntity reloaded = em.find(CustomerJpaEntity.class, new CustomerJpaKey(userId, TENANT));
            assertNotNull(reloaded);
            assertEquals("login@example.test", reloaded.getEmail());
        });
    }

    @Test
    void auditIsGeneratedAndPreservedWhenSavingDetachedDomain() {
        Customer saved = customers.create(customer("audit@example.test"));
        var key = new CustomerJpaKey(saved.getKey().getId(), TENANT);
        var tx = new TransactionTemplate(transactionManager);
        var original = tx.execute(status -> {
            CustomerJpaEntity entity = em.find(CustomerJpaEntity.class, key);
            assertNotNull(entity.getCreatedAt());
            assertEquals(entity.getCreatedAt(), entity.getUpdatedAt());
            return entity.getCreatedAt();
        });

        Customer detached = customerRepository.findByIdAndTenantId(key.getId(), TENANT).orElseThrow();
        detached.setName("Updated name");
        customerRepository.update(detached);

        tx.executeWithoutResult(status -> {
            CustomerJpaEntity entity = em.find(CustomerJpaEntity.class, key);
            assertEquals(original, entity.getCreatedAt());
            assertTrue(entity.getUpdatedAt().isAfter(original));
            assertEquals("Updated name", entity.getName());
        });
    }

    @Test
    void priceRoundTripUsesExistingPlanWithoutCascadingDomainChanges() {
        String tenant = "price-mapping";
        Plan plan = planRepository.create(new Plan(new PlanKey(UUID.randomUUID(), tenant),
                PlanType.PRO, "Original plan", 14, true));
        plan.setName("Local change that must not be persisted through a price");
        PlanPrice saved = priceRepository.create(new PlanPrice(new PlanPriceKey(UUID.randomUUID(), tenant),
                plan, Period.MONTHLY, 2500, true));
        assertEquals("Original plan", saved.getPlan().getName());
        assertEquals(plan.getKey(), saved.getPlan().getKey());

        PlanPrice reloaded = priceRepository.findByPlanTypeAndPeriodAndTenantId("PRO", Period.MONTHLY, tenant)
                .orElseThrow();
        assertEquals(14, reloaded.getPlan().getTrialDays());
        reloaded.setPriceCents(3000);
        priceRepository.update(reloaded);
        var prices = priceRepository.listActivePricesByTenantId(tenant);
        assertEquals(1, prices.size());
        assertEquals(saved.getKey(), prices.getFirst().getKey());
        assertEquals(3000, prices.getFirst().getPriceCents());
        assertEquals("Original plan", prices.getFirst().getPlan().getName());
        assertTrue(priceRepository.listActivePricesByTenantId("missing-tenant").isEmpty());
        assertEquals("Original plan", planRepository.listActivePlansByTenantId(tenant).getFirst().getName());
    }

    @Test
    void updateRejectsMissingKeys() {
        Customer customer = new Customer();
        customer.setKey(new CustomerKey(UUID.randomUUID(), "missing"));
        Plan plan = new Plan(new PlanKey(UUID.randomUUID(), "missing"), PlanType.BASIC, "Missing", 0, true);
        PlanPrice price = new PlanPrice(new PlanPriceKey(UUID.randomUUID(), "missing"), plan, Period.MONTHLY, 1000, true);

        assertThrows(com.example.signeasy.domain.common.BusinessException.class, () -> customerRepository.update(customer));
        assertThrows(com.example.signeasy.domain.common.BusinessException.class, () -> planRepository.update(plan));
        assertThrows(com.example.signeasy.domain.common.BusinessException.class, () -> priceRepository.update(price));
        assertTrue(customerRepository.findByIdAndTenantId(customer.getKey().getId(), "missing").isEmpty());
        assertTrue(planRepository.listActivePlansByTenantId("missing").isEmpty());
        assertTrue(priceRepository.listActivePricesByTenantId("missing").isEmpty());
    }

    @Test
    void duplicateCreatesFailAndPlanUpdatePreservesIdentity() {
        String tenant = "explicit-create";
        Customer customer = customer("duplicate@example.test");
        customer.setKey(new CustomerKey(UUID.randomUUID(), tenant));
        customerRepository.create(customer);
        Plan plan = planRepository.create(new Plan(new PlanKey(UUID.randomUUID(), tenant), PlanType.BASIC, "Original", 0, true));
        PlanPrice price = priceRepository.create(new PlanPrice(new PlanPriceKey(UUID.randomUUID(), tenant), plan, Period.MONTHLY, 1000, true));

        customer.setName("Duplicate");
        plan.setName("Duplicate");
        price.setPriceCents(2000);
        // Each adapter call owns its transaction, so these assertions include commit.
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> customerRepository.create(customer));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> planRepository.create(plan));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> priceRepository.create(price));
        assertEquals("Test Customer", customerRepository.findByIdAndTenantId(customer.getKey().getId(), tenant).orElseThrow().getName());
        assertEquals("Original", planRepository.findByPlanTypeAndTenantId("BASIC", tenant).orElseThrow().getName());
        assertEquals(1000, priceRepository.findByPlanTypeAndPeriodAndTenantId("BASIC", Period.MONTHLY, tenant).orElseThrow().getPriceCents());

        plan.setName("Updated");
        planRepository.update(plan);
        Plan reloaded = planRepository.findByPlanTypeAndTenantId("BASIC", tenant).orElseThrow();
        assertEquals(plan.getKey(), reloaded.getKey());
        assertEquals("Updated", reloaded.getName());
    }

    private Customer customer(String email) {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail(email);
        return customer;
    }
}
