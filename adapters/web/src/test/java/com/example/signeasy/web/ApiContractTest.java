package com.example.signeasy.web;

import com.example.signeasy.application.services.SubscriptionService;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.model.Subscription;
import com.example.signeasy.domain.model.SubscriptionKey;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.customer.CustomerKey;
import com.example.signeasy.domain.model.plan.*;
import com.example.signeasy.web.controller.SubscriptionController;
import com.example.signeasy.web.dto.*;
import com.example.signeasy.web.mapper.ApiMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiContractTest {
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();

    @Test
    void subscriptionEndpointPreservesJsonAndConvertsRequestEnums() throws Exception {
        var id = UUID.randomUUID();
        var plan = new Plan(new PlanKey(id, "tenant"), PlanType.BASIC, "Basic", 7, true);
        var price = new PlanPrice(new PlanPriceKey(id, "tenant"), plan, Period.MONTHLY, 1000, true);
        var customer = new Customer(new CustomerKey(id, "tenant"), "Customer", "c@example.test", Customer.Status.ACTIVE);
        var subscription = new Subscription(new SubscriptionKey(id, "tenant"), customer, price,
                Subscription.Status.ACTIVE, LocalDate.of(2026, 1, 1), null, LocalDate.of(2026, 2, 1), null);
        var service = mock(SubscriptionService.class);
        when(service.subscribe(id, "BASIC", Period.MONTHLY)).thenReturn(subscription);
        var mvc = MockMvcBuilders.standaloneSetup(new SubscriptionController(service))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(json)).build();

        mvc.perform(post("/api/subscriptions").contentType("application/json")
                        .content("{\"customerId\":\"" + id + "\",\"planType\":\"BASIC\",\"period\":\"MONTHLY\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json(json.writeValueAsString(subscription), true));
        verify(service).subscribe(id, "BASIC", Period.MONTHLY);

        var response = ApiMapper.toResponse(subscription);
        customer.setName("Changed");
        plan.setName("Changed");
        assertEquals("Customer", response.customer().name());
        assertEquals("Basic", response.planPrice().plan().name());
    }

    @Test
    void contractRecordsContainOnlyApiAndJdkTypes() {
        for (var type : new Class<?>[]{
                KeyResponse.class, CustomerDtos.CustomerResponse.class, CustomerDtos.CreateCustomerRequest.class,
                PlanDtos.PlanResponse.class, PlanDtos.PlanPriceResponse.class,
                PlanDtos.CreatePlanRequest.class, PlanDtos.CreatePlanPriceRequest.class,
                SubscriptionDtos.SubscriptionResponse.class, SubscriptionDtos.SubscribeRequest.class,
                SubscriptionDtos.ChangePlanRequest.class}) {
            for (var component : type.getRecordComponents()) {
                var fieldType = component.getType();
                assertTrue(fieldType.isPrimitive() || fieldType.getPackageName().startsWith("java.")
                                || fieldType.getPackageName().equals("com.example.signeasy.web.dto"),
                        () -> type.getSimpleName() + "." + component.getName() + " exposes " + fieldType);
            }
        }
    }
}
