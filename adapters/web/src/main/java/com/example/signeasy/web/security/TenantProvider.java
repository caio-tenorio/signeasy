package com.example.signeasy.web.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TenantProvider {
    public String getCurrentTenantId() {
        String tenantId = TenantContextHolder.get();
        if (tenantId != null) {
            return tenantId;
        }
        return extractTenantIdFromJwt();
    }

    public String extractTenantIdFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Object tenantIdObj = jwt.getClaim("tenantId");
            if (tenantIdObj instanceof List<?> list && !list.isEmpty()) {
                return String.valueOf(list.getFirst());
            }
            if (tenantIdObj instanceof String str && !str.isBlank()) {
                return str;
            }
        }
        throw new IllegalStateException("TenantId não encontrado no JWT");
    }

    public String getCurrentUserSub() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("sub");
        }
        throw new IllegalStateException("sub não encontrado no JWT");
    }
}
