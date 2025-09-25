package com.example.signeasy.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class LoggingMdcFilter implements Filter {
    private static final String CORRELATION_ID_HEADER = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest httpReq) {
                // CorrelationId
                String correlationId = Optional.ofNullable(httpReq.getHeader(CORRELATION_ID_HEADER))
                        .filter(h -> !h.isBlank())
                        .orElse(UUID.randomUUID().toString());
                MDC.put("correlationId", correlationId);

                // IP
                String ip = Optional.ofNullable(httpReq.getHeader("X-Forwarded-For"))
                        .orElse(request.getRemoteAddr());
                MDC.put("ip", ip);

                // Usuário autenticado
                String user = extractUser();
                if (user != null) {
                    MDC.put("user", user);
                }
            }
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String extractUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            return auth.getName();
        }
        return null;
    }
}

