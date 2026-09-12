package com.example.signeasy.web;

import com.example.signeasy.domain.common.*;
import com.example.signeasy.domain.model.customer.*;
import com.example.signeasy.domain.model.plan.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.aMapWithSize;

@SpringBootTest(classes = ApiApplication.class, properties = "spring.jpa.open-in-view=false")
@AutoConfigureMockMvc
@Testcontainers
class SubscriptionHttpIT {
    @Container static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16");
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }
    @MockBean JwtDecoder jwtDecoder;
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactions;
    @Autowired ObjectMapper json;

    @Test void createChangeAndCancelSerializeAfterServiceTransaction() throws Exception {
        UUID customerId = UUID.randomUUID();
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            em.persist(new Customer(new CustomerKey(customerId, "http"), "Customer", "http@example.test", Customer.Status.ACTIVE));
            for (PlanType type : new PlanType[]{PlanType.BASIC, PlanType.PRO}) {
                Plan plan = new Plan(new PlanKey(UUID.randomUUID(), "http"), type, type.name(), 0, true);
                em.persist(plan);
                em.persist(new PlanPrice(new PlanPriceKey(UUID.randomUUID(), "http"), plan, Period.MONTHLY, 1000, true));
            }
        });
        var authentication = jwt().jwt(token -> token.subject(customerId.toString()).claim("tenantId", "http"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        String body = mvc.perform(post("/api/subscriptions").with(authentication).contentType("application/json")
                        .content("{\"customerId\":\"" + customerId + "\",\"planType\":\"BASIC\",\"period\":\"MONTHLY\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.key", aMapWithSize(2)))
                .andExpect(jsonPath("$.customerId").doesNotExist()).andExpect(jsonPath("$.planPriceId").doesNotExist())
                .andExpect(jsonPath("$.customer.key.id").value(customerId.toString()))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(body).path("key").path("id").asText();
        UUID.fromString(id);
        mvc.perform(post("/api/subscriptions/" + id + "/change-plan").with(authentication)
                        .contentType("application/json").content("{\"newPlanType\":\"PRO\",\"newPeriod\":\"MONTHLY\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.key.id").value(id))
                .andExpect(jsonPath("$.planPrice.plan.planType").value("PRO"));
        mvc.perform(post("/api/subscriptions/" + id + "/cancel").with(authentication))
                .andExpect(status().isOk()).andExpect(jsonPath("$.key.id").value(id))
                .andExpect(jsonPath("$.status").value("CANCELED")).andExpect(jsonPath("$.endDate").isNotEmpty());
    }
}
