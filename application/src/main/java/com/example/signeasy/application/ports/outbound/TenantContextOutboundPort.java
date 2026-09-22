package com.example.signeasy.application.ports.outbound;

/**
 * Exposes the tenant associated with the current request to the application layer.
 */
public interface TenantContextOutboundPort {
    /**
     * @return the tenant identifier for the current request
     * @throws IllegalStateException if no tenant is associated with the current request
     */
    String currentTenantId();
}
