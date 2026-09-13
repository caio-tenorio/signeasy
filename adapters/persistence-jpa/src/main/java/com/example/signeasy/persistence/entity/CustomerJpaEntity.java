package com.example.signeasy.persistence.entity;

import jakarta.persistence.*;
import com.example.signeasy.domain.model.customer.Customer.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(name = "uk_customer_email", columnNames = {"tenant_id", "email"}))
public class CustomerJpaEntity {
    @EmbeddedId
    private CustomerJpaKey key;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void initializeAudit() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }

    public CustomerJpaEntity() {}

    public CustomerJpaEntity(CustomerJpaKey key, String name, String email, Status status) {
        this.key = key;
        this.name = name;
        this.email = email;
        this.status = status;
    }

    public CustomerJpaKey getKey() {
        return key;
    }

    public void setKey(CustomerJpaKey key) {
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

}
