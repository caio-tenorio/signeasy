package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.CustomerRepositoryPort;
import com.example.signeasy.application.ports.TenantContext;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.customer.Customer.Status;
import com.example.signeasy.domain.model.customer.CustomerKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;
    @Mock
    private TenantContext tenantContext;

    @InjectMocks
    private CustomerService customerService;

    private final String tenantId = "tenant-abc";

    @Test
    void createThrowsWhenEmailAlreadyExists() {
        Customer existing = buildCustomer(UUID.randomUUID(), "john@example.com");
        Customer input = buildCustomer(UUID.randomUUID(), "john@example.com");

        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByEmailAndTenantId("john@example.com", tenantId))
                .thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> customerService.create(input));
        verify(customerRepositoryPort, never()).save(any());
    }

    @Test
    void createPersistsCustomerWhenEmailIsUnique() {
        Customer input = buildCustomer(UUID.randomUUID(), "jane@example.com");
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByEmailAndTenantId("jane@example.com", tenantId))
                .thenReturn(Optional.empty());
        when(customerRepositoryPort.save(input)).thenReturn(input);

        Customer saved = customerService.create(input);

        assertSame(input, saved);
        verify(customerRepositoryPort).save(input);
    }

    @Test
    void findByEmailReturnsCustomerWhenPresent() {
        Customer customer = buildCustomer(UUID.randomUUID(), "found@example.com");
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByEmailAndTenantId("found@example.com", tenantId))
                .thenReturn(Optional.of(customer));

        Customer result = customerService.findByEmail("found@example.com");

        assertSame(customer, result);
    }

    @Test
    void findByEmailThrowsWhenMissing() {
        when(tenantContext.currentTenantId()).thenReturn(tenantId);
        when(customerRepositoryPort.findByEmailAndTenantId("missing@example.com", tenantId))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> customerService.findByEmail("missing@example.com"));
    }

    @Test
    void provisionIfNotExistsCreatesCustomerWhenMissing() {
        UUID userId = UUID.randomUUID();
        when(customerRepositoryPort.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.empty());

        customerService.provisionIfNotExists(userId.toString(), tenantId, "user@example.com", "User");

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepositoryPort).save(captor.capture());
        Customer saved = captor.getValue();

        assertEquals(userId, saved.getKey().getId());
        assertEquals(tenantId, saved.getKey().getTenantId());
        assertEquals("user@example.com", saved.getEmail());
        assertEquals("User", saved.getName());
        assertEquals(Status.ACTIVE, saved.getStatus());
    }

    @Test
    void provisionIfNotExistsDoesNothingWhenCustomerAlreadyExists() {
        UUID userId = UUID.randomUUID();
        Customer existing = buildCustomer(userId, "existing@example.com");
        when(customerRepositoryPort.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(existing));

        customerService.provisionIfNotExists(userId.toString(), tenantId, "existing@example.com", "Existing");

        verify(customerRepositoryPort, never()).save(any());
    }

    private Customer buildCustomer(UUID id, String email) {
        CustomerKey key = new CustomerKey(id, tenantId);
        Customer customer = new Customer();
        customer.setKey(key);
        customer.setEmail(email);
        customer.setName("Test User");
        customer.setStatus(Status.ACTIVE);
        return customer;
    }
}
