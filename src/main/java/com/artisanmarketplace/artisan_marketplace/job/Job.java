package com.artisanmarketplace.artisan_marketplace.job;

import com.artisanmarketplace.artisan_marketplace.trade.Trade;
import com.artisanmarketplace.artisan_marketplace.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Getter
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_artisan_id")
    private User assignedArtisan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_required_id", nullable = false)
    private Trade tradeRequired;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double latitude;
    private Double longitude;

    @Column(name = "location_name")
    private String locationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobOrigin origin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(nullable = false)
    private Double budget;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Job() {
        // required by JPA
    }

    public Job(User client, Trade tradeRequired, String title, String description,
               Double latitude, Double longitude, String locationName,
               JobOrigin origin, Double budget) {
        this.client = client;
        this.tradeRequired = tradeRequired;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.origin = origin;
        this.budget = budget;
        // Initial status depends on origin:
        // - OPEN_POST  → OPEN (waiting for responses)
        // - DIRECT_SEARCH → REQUESTED (specific artisan was chosen)
        this.status = (origin == JobOrigin.OPEN_POST) ? JobStatus.OPEN : JobStatus.REQUESTED;
    }

    // Intentional, controlled mutation — not a blanket setter
    public void transitionTo(JobStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new IllegalStateException(
                    "Invalid transition: " + this.status + " -> " + target);
        }
        this.status = target;
    }

    public void assignArtisan(User artisan) {
        this.assignedArtisan = artisan;
    }
}