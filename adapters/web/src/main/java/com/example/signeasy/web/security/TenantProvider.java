package com.example.signeasy.web.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class TenantProvider {
    public String getCurrentTenantId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Object tenantIdObj = jwt.getClaim("tenantId");
            if (tenantIdObj instanceof java.util.List<?> list && !list.isEmpty()) {
                return String.valueOf(list.get(0));
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