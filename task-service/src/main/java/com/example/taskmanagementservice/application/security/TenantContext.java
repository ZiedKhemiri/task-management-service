package com.example.taskmanagementservice.application.security;

/**
 * Holds the current request's tenant (enterprise) info, resolved from the JWT's
 * {@code enterprise_id} claim by {@link com.example.taskmanagementservice.infrastructure.config.TenantFilter}.
 * <p>
 * SUPER_ADMIN callers bypass tenant scoping entirely (see {@link #isSuperAdmin()}),
 * mirroring how {@code demo}'s SUPER_ADMIN role is treated as cross-enterprise.
 */
public final class TenantContext {

    private record Tenant(Long enterpriseId, boolean superAdmin) {}

    private static final ThreadLocal<Tenant> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(Long enterpriseId, boolean superAdmin) {
        CURRENT.set(new Tenant(enterpriseId, superAdmin));
    }

    public static Long getEnterpriseId() {
        Tenant tenant = CURRENT.get();
        return tenant != null ? tenant.enterpriseId() : null;
    }

    public static boolean isSuperAdmin() {
        Tenant tenant = CURRENT.get();
        return tenant != null && tenant.superAdmin();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
