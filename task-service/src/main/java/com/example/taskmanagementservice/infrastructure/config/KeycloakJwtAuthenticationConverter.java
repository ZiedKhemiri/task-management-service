package com.example.taskmanagementservice.infrastructure.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps Keycloak's {@code realm_access.roles} claim (e.g. ADMIN, USER) to
 * Spring Security authorities ({@code ROLE_ADMIN}, {@code ROLE_USER}), on top of
 * whatever the default JWT scope-based authorities converter already produces.
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        Collection<GrantedAuthority> authorities = Stream.of(
                new JwtGrantedAuthoritiesConverter().convert(source),
                extractRealmRoles(source)
        ).flatMap(Collection::stream).collect(Collectors.toSet());

        return new JwtAuthenticationToken(source, authorities);
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return Collections.emptySet();

        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null) return Collections.emptySet();

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }
}
