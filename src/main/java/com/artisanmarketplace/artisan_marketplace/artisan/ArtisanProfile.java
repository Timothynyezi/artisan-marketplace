package com.artisanmarketplace.artisan_marketplace.artisan;

import com.artisanmarketplace.artisan_marketplace.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "artisan_profiles")
@Getter

public class ArtisanProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "hourly_rate")
    private Double hourlyRate;


    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_name")
    private String locationName; // e.g., "Cape Town CBD"

    @Column(name = "avg_rating")
    private Double avgRating = 0.0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected ArtisanProfile() {
        // required by JPA
    }

    public ArtisanProfile(User user, String bio, Double hourlyRate) {
        this.user = user;
        this.bio = bio;
        this.hourlyRate = hourlyRate;
    }

    // Convenience method to check if profile is complete enough for matching
    public boolean isComplete() {
        return bio != null && hourlyRate != null && latitude != null && longitude != null;
    }


    public double getPortfolioCompletenessScore() {
        int score = 0;
        if (bio != null && !bio.isBlank()) score += 1;
        if (hourlyRate != null && hourlyRate > 0) score += 1;
        if (locationName != null && !locationName.isBlank()) score += 1;
        if (verified) score += 1;
        return Math.min(1.0, score / 4.0); // Normalize to 0-1
    }
    // Intentional, controlled mutation methods - not blanket setters
    public void updateProfile(String bio, Double hourlyRate, String locationName) {
        this.bio = bio;
        this.hourlyRate = hourlyRate;
        this.locationName = locationName;
    }

    public void recordNewRating(double newRating) {
        double totalScore = (this.avgRating * this.reviewCount) + newRating;
        this.reviewCount += 1;
        this.avgRating = totalScore / this.reviewCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtisanProfile that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // stable, doesn't depend on mutable fields
    }
}