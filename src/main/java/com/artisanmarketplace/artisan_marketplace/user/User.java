package com.artisanmarketplace.artisan_marketplace.user;

import jakarta.persistence.*;
import org.hibernate.annotations.Audited;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    protected User() {
        // required by JPA - it needs a no-arg constructor to build objects via reflection

    }
    public User(String email, String passwordHash, Role role, String fullName, String phone) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
    }
    // Getters only - no setters yet
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public String getFullName() { return fullName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
