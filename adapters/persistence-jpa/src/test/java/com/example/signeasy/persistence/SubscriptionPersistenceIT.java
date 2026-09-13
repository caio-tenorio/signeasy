package com.example.signeasy.persistence;

import com.example.signeasy.persistence.entity.*;
import com.example.signeasy.persistence.mapper.*;

import com.example.signeasy.application.ports.*;
import com.example.signeasy.application.services.SubscriptionService;
import com.example.signeasy.domain.common.*;
import com.example.signeasy.domain.model.*;
import com.example.signeasy.domain.model.customer.*;
import com.example.signeasy.domain.model.plan.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SubscriptionPersistenceIT.Config.class, properties = {
        "spring.jpa.hibernate.ddl-auto=validate", "spring.jpa.open-in-view=false"})
@Testcontainers
class SubscriptionPersistenceIT {
    @Container static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16");
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.example.signeasy.persistence.entity")
    @EnableJpaRepositories("com.example.signeasy.persistence.jpa")
    @ComponentScan({"com.example.signeasy.persistence.adapter", "com.example.signeasy.persistence.mapper"})
    static class Config {
        @Bean SubscriptionService subscriptions(CustomerRepositoryPort c, PlanPriceRepositoryPort p, SubscriptionRepositoryPort s) {
            return new SubscriptionService(c, p, s, () -> "alpha");
        }
    }
    @Autowired SubscriptionService service;
    @Autowired SubscriptionRepositoryPort subscriptions;
    @Autowired EntityManager em;
    @Autowired CustomerJpaMapper customerMapper;
    @Autowired PlanPriceJpaMapper priceMapper;
    @Autowired PlatformTransactionManager transactionManager;

    private void transaction(Runnable action) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            action.run(); em.flush(); em.clear();
        });
    }
    private Customer customer(UUID id, String tenant) {
        return new Customer(new CustomerKey(id, tenant), "Customer", id + "@example.test", Customer.Status.ACTIVE);
    }
    private PlanPrice planPrice(String tenant, PlanType type) {
        Plan p = new Plan(new PlanKey(UUID.randomUUID(), tenant), type, type.name(), 7, true);
        em.persist(new PlanJpaMapper().toEntity(p));
        PlanPrice price = new PlanPrice(new PlanPriceKey(UUID.randomUUID(), tenant), p, Period.MONTHLY, 1000, true);
        em.persist(priceMapper.toEntity(price));
        return price;
    }

    @Test void lifecyclePersistsWithoutChangingIdentity() {
        UUID customerId = UUID.randomUUID();
        transaction(() -> {
            em.persist(customerMapper.toEntity(customer(customerId, "alpha")));
            planPrice("alpha", PlanType.BASIC);
            planPrice("alpha", PlanType.PRO);
        });
        Subscription created = service.subscribe(customerId, "BASIC", Period.MONTHLY);
        UUID id = created.getKey().getId();
        // Returned domain graphs must remain usable after the transaction closes.
        assertEquals(PlanType.BASIC, created.getPlanPrice().getPlan().getPlanType());
        assertEquals(customerId, created.getCustomer().getKey().getId());
        Subscription detached = subscriptions.findByIdAndTenantId(id, "alpha").orElseThrow();
        assertEquals(PlanType.BASIC, detached.getPlanPrice().getPlan().getPlanType());
        assertEquals(customerId, detached.getCustomer().getKey().getId());
        assertNotNull(id);
        assertNotEquals(customerId, id);
        Subscription second = service.subscribe(customerId, "BASIC", Period.MONTHLY);
        assertNotEquals(id, second.getKey().getId());
        transaction(() -> {
            assertTrue(subscriptions.findByIdAndTenantId(customerId, "alpha").isEmpty());
            assertTrue(subscriptions.findByIdAndTenantId(id, "beta").isEmpty());
            assertEquals(Subscription.Status.IN_TRIAL, subscriptions.findByIdAndTenantId(id, "alpha").orElseThrow().getStatus());
        });
        service.changePlan(id, "PRO", Period.MONTHLY);
        transaction(() -> {
            Subscription changed = subscriptions.findByIdAndTenantId(id, "alpha").orElseThrow();
            assertEquals(new SubscriptionKey(id, "alpha"), changed.getKey());
            assertEquals(PlanType.PRO, changed.getPlanPrice().getPlan().getPlanType());
            assertEquals(PlanType.BASIC, subscriptions.findByIdAndTenantId(second.getKey().getId(), "alpha").orElseThrow().getPlanPrice().getPlan().getPlanType());
            assertEquals(2L, em.createQuery("select count(s) from SubscriptionJpaEntity s", Long.class).getSingleResult());
        });
        service.cancel(id);
        transaction(() -> {
            Subscription canceled = subscriptions.findByIdAndTenantId(id, "alpha").orElseThrow();
            assertEquals(Subscription.Status.CANCELED, canceled.getStatus());
            assertEquals(LocalDate.now(), canceled.getEndDate());
            assertEquals(new SubscriptionKey(id, "alpha"), canceled.getKey());
        });
        // The same UUID can identify a different subscription in another tenant.
        transaction(() -> {
            Customer c = customer(UUID.randomUUID(), "beta");
            PlanPrice p = planPrice("beta", PlanType.BASIC);
            em.persist(customerMapper.toEntity(c));
            Subscription other = new Subscription();
            other.setKey(new SubscriptionKey(id, "beta"));
            other.setCustomer(c); other.setPlanPrice(p);
            subscriptions.save(other);
        });
        transaction(() -> {
            assertEquals("beta", subscriptions.findByIdAndTenantId(id, "beta").orElseThrow().getCustomer().getKey().getTenantId());
            assertEquals(Subscription.Status.CANCELED, subscriptions.findByIdAndTenantId(id, "alpha").orElseThrow().getStatus());
        });
    }
}
