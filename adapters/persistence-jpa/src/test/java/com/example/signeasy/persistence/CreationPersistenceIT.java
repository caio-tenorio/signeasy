package com.example.signeasy.persistence;

import com.example.signeasy.application.ports.CustomerRepositoryPort;
import com.example.signeasy.application.ports.PlanRepositoryPort;
import com.example.signeasy.application.services.CustomerService;
import com.example.signeasy.application.services.PlanService;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.customer.CustomerKey;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.domain.model.plan.PlanKey;
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
    @EntityScan("com.example.signeasy.domain.model")
    @EnableJpaRepositories("com.example.signeasy.persistence.jpa")
    @ComponentScan("com.example.signeasy.persistence.adapter")
    static class Config {
        @Bean
        PlanService plans(PlanRepositoryPort repository) {
            return new PlanService(repository, () -> TENANT);
        }

        @Bean
        CustomerService customers(CustomerRepositoryPort repository) {
            return new CustomerService(repository, () -> TENANT);
        }
    }

    @Autowired PlanService plans;
    @Autowired CustomerService customers;
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
            Plan reloaded = em.find(Plan.class, new PlanKey(id, TENANT));
            assertNotNull(reloaded);
            assertEquals("Plano Teste", reloaded.getName());
            assertEquals(1, reloaded.getTrialDays());
            assertNull(em.find(Plan.class, new PlanKey(id, "other-tenant")));
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
                Customer reloaded = em.find(Customer.class, new CustomerKey(saved.getKey().getId(), TENANT));
                assertNotNull(reloaded);
                assertEquals(saved.getEmail(), reloaded.getEmail());
                assertNotNull(reloaded.getCreatedAt());
                assertNull(em.find(Customer.class, new CustomerKey(saved.getKey().getId(), "other-tenant")));
            }
        });
    }

    @Test
    void provisionCustomerCommitsIdentityFromLogin() {
        UUID userId = UUID.randomUUID();
        customers.provisionIfNotExists(userId.toString(), TENANT, "login@example.test", "Login User");

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Customer reloaded = em.find(Customer.class, new CustomerKey(userId, TENANT));
            assertNotNull(reloaded);
            assertEquals("login@example.test", reloaded.getEmail());
        });
    }

    private Customer customer(String email) {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail(email);
        return customer;
    }
}
