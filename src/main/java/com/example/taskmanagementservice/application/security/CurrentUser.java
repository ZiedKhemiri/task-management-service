package com.example.taskmanagementservice.application.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public record CurrentUser(String id, String username, boolean admin) {

    public static CurrentUser from(Authentication authentication) {
        String username = authentication.getName();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String preferredUsername = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (preferredUsername != null) username = preferredUsername;
        }
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return new CurrentUser(authentication.getName(), username, admin);
    }
}
