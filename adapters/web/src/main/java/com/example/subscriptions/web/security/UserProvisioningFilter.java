package com.example.subscriptions.web.security;

import com.example.subscriptions.application.services.CustomerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserProvisioningFilter extends OncePerRequestFilter {
    private final CustomerService customerService;
    private final TenantProvider tenantProvider;

    public UserProvisioningFilter(CustomerService customerService, TenantProvider tenantProvider) {
        this.customerService = customerService;
        this.tenantProvider = tenantProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String sub = tenantProvider.getCurrentUserSub();
            String tenantId = tenantProvider.getCurrentTenantId();
            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("name");
            customerService.provisionIfNotExists(sub, tenantId, email, name);
        }
        filterChain.doFilter(request, response);
    }
}

