package com.example.signeasy.web.security;

import com.example.signeasy.application.ports.outbound.TenantContextOutboundPort;
import org.springframework.stereotype.Component;

@Component
public class RequestTenantContext implements TenantContextOutboundPort {
    @Override
    public String currentTenantId() {
        String tenantId = TenantContextHolder.get();
        if (tenantId == null) {
            throw new IllegalStateException("TenantId não disponível no contexto da requisição");
        }
        return tenantId;
    }
}
