package com.example.signeasy.web.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * Interface para abstrair a extração de GrantedAuthority de um token JWT.
 * Permite que diferentes implementações sejam fornecidas para diferentes provedores de identidade.
 */
@FunctionalInterface
public interface JwtAuthoritiesExtractor {

    Collection<GrantedAuthority> extractAuthorities(Jwt jwt);

}
