package com.example.taskmanagementservice.infrastructure.config;

import com.example.taskmanagementservice.application.security.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resolves the calling user's tenant (enterprise) from the JWT's {@code enterprise_id}
 * claim and a {@code ROLE_SUPER_ADMIN} authority, and populates {@link TenantContext}
 * for the duration of the request. Runs after Spring Security's bearer-token
 * authentication filter, so {@link JwtAuthenticationToken} is already on the context
 * by the time this runs.
 * <p>
 * Requests without an authenticated JWT (e.g. public Swagger URLs) pass through
 * untouched; {@code TaskController}/{@code TaskService} only consult
 * {@link TenantContext} on endpoints that already require authentication.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                boolean superAdmin = jwtAuth.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
                TenantContext.set(extractEnterpriseId(jwt), superAdmin);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private Long extractEnterpriseId(Jwt jwt) {
        Object claim = jwt.getClaim("enterprise_id");
        if (claim == null) return null;
        if (claim instanceof Number number) return number.longValue();
        try {
            return Long.valueOf(claim.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
