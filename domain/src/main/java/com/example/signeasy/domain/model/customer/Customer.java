package com.example.signeasy.domain.model.customer;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(name = "uk_customer_email", columnNames = {"tenant_id", "email"}))
public class Customer {
    @EmbeddedId
    private CustomerKey key;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private final Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }

    public enum Status {ACTIVE, SUSPENDED}

    public Customer() {}

    public Customer(CustomerKey key, String name, String email, Status status) {
        this.key = key;
        this.name = name;
        this.email = email;
        this.status = status;
    }

    public CustomerKey getKey() {
        return key;
    }

    public void setKey(CustomerKey key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}