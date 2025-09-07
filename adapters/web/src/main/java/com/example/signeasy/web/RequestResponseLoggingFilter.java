package com.example.signeasy.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RequestResponseLoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            logRequest(wrappedRequest);
            logResponse(wrappedResponse);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        StringBuilder msg = new StringBuilder();
        msg.append("[REQUEST] ")
                .append(request.getMethod()).append(" ")
                .append(request.getRequestURL());
        if (request.getQueryString() != null) {
            msg.append("?").append(request.getQueryString());
        }

        String body = getPayload(request.getContentAsByteArray());
        if (!body.isEmpty()) {
            msg.append(" | Body: ").append(body);
        }
        logger.info(msg.toString());
    }

    private String getClientIp(ContentCachingRequestWrapper request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // Pode conter múltiplos IPs (quando há vários proxies), pega o primeiro
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        StringBuilder msg = new StringBuilder();
        msg.append("[RESPONSE] Status: ").append(response.getStatus());
        String body = getPayload(response.getContentAsByteArray());
        if (!body.isEmpty()) {
            msg.append(" | Body: ").append(body);
        }
        logger.info(msg.toString());
    }

    private String getPayload(byte[] buf) {
        if (buf == null || buf.length == 0) return "";
        return new String(buf, StandardCharsets.UTF_8).replaceAll("\s+", " ").trim();
    }
}

