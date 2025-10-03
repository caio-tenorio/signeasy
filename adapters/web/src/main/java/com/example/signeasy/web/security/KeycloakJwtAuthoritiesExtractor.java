package com.example.signeasy.web.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementação de {@link JwtAuthoritiesExtractor} para o Keycloak.
 * Extrai roles dos claims 'realm_access' e 'resource_access'.
 */
public class KeycloakJwtAuthoritiesExtractor implements JwtAuthoritiesExtractor {

    private final String clientId;

    public KeycloakJwtAuthoritiesExtractor(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> realmRoles = getRolesFromClaim(realmAccess);

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        List<String> clientRoles = new ArrayList<>();
        if (resourceAccess != null && resourceAccess.containsKey(clientId)) {
            Object clientData = resourceAccess.get(clientId);
            if (clientData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientMap = (Map<String, Object>) clientData;
                clientRoles = getRolesFromClaim(clientMap);
            }
        }

        return Stream.concat(realmRoles.stream(), clientRoles.stream())
                .distinct()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    private List<String> getRolesFromClaim(Map<String, Object> claim) {
        if (claim == null) {
            return List.of();
        }

        Object rolesObj = claim.getOrDefault("roles", List.of());
        if (rolesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) rolesObj;
            return roles;
        }
        return List.of();
    }
}
