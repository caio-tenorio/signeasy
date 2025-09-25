package com.example.signeasy.web.security;


public final class TenantContextHolder {
    // I'm using ThreadLocal instead of InheritableThreadLocal to prevent possibles leaks of child threads being reused by pools
    // I clean the context in a finally block, but for child threads this would not be enough
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String get() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
