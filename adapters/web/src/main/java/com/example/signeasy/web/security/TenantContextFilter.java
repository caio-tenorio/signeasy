package com.example.signeasy.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Populates the {@link TenantContextHolder} once the request is authenticated so that
 * downstream components can access the tenant without reading the JWT again.
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {
    private final TenantProvider tenantProvider;

    public TenantContextFilter(TenantProvider tenantProvider) {
        this.tenantProvider = tenantProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (resolveJwt() != null) {
            try {
                TenantContextHolder.set(tenantProvider.extractTenantIdFromJwt());
                filterChain.doFilter(request, response);
            } finally {
                TenantContextHolder.clear();
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private Jwt resolveJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }
}
