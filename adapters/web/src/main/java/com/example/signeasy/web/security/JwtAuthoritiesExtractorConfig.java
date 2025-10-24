package com.example.signeasy.web.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtAuthoritiesExtractorConfig {

    @Bean
    @ConditionalOnProperty(name = "app.security.jwt.provider", havingValue = "keycloak", matchIfMissing = true)
    public JwtAuthoritiesExtractor keycloakJwtAuthoritiesExtractor(
            @Value("${app.security.jwt.keycloak.client-id}") String clientId) {
        return new KeycloakJwtAuthoritiesExtractor(clientId);
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.jwt.provider", havingValue = "standard")
    public JwtAuthoritiesExtractor standardJwtAuthoritiesExtractor() {
        return new StandardJwtAuthoritiesExtractor();
    }
}
