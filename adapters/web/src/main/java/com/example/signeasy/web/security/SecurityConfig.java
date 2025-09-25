package com.example.signeasy.web.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TenantContextFilter tenantContextFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // liberar health e docs
                        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger/**").permitAll()
                        // RBAC de exemplo
                        .requestMatchers(HttpMethod.POST, "/api/plans/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/plans/**").hasAnyRole("ADMIN", "TENANT_ADMIN", "USER")
                        .requestMatchers("/api/subscriptions/**").hasAnyRole("TENANT_ADMIN", "USER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));

        http.addFilterAfter(tenantContextFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<TenantContextFilter> disableTenantContextFilterRegistration(TenantContextFilter filter) {
        FilterRegistrationBean<TenantContextFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Converte roles do Keycloak em authorities do Spring:
     * - realm_access.roles -> ROLE_*
     * - resource_access.<clientId>.roles -> ROLE_*
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // 1) Roles no realm (realm_access.roles)
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> realmRoles = new ArrayList<>();
        if (realmAccess != null) {
            Object rolesObj = realmAccess.getOrDefault("roles", List.of());
            if (rolesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) rolesObj;
                realmRoles = roles;
            }
        }

        // 2) Roles no client (resource_access.<clientId>.roles)
        String clientId = "subscriptions-api"; // ajuste aqui para o seu Client ID no Keycloak
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

        List<String> clientRoles = new ArrayList<>();
        if (resourceAccess != null) {
            Object clientSectionObj = resourceAccess.get(clientId);
            if (clientSectionObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientSection = (Map<String, Object>) clientSectionObj;
                Object clientRolesObj = clientSection.getOrDefault("roles", List.of());
                if (clientRolesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) clientRolesObj;
                    clientRoles = roles;
                }
            }
        }

        // 3) Junta e mapeia para ROLE_*
        Stream<String> allRolesStream = Stream.concat(realmRoles.stream(), clientRoles.stream()).distinct();

        List<GrantedAuthority> authorities = new ArrayList<>();
        allRolesStream.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        return authorities;
    }
}
