package com.example.signeasy.web.dto;

/** Values owned by the HTTP contract, independent of domain enums. */
public final class ApiTypes {
    private ApiTypes() {}

    public enum PlanType { BASIC, PRO }
    public enum Period { MONTHLY, YEARLY }
    public enum CustomerStatus { ACTIVE, SUSPENDED }
    public enum SubscriptionStatus { IN_TRIAL, ACTIVE, SUSPENDED, CANCELED, EXPIRED }
}
