package com.artisanmarketplace.artisan_marketplace.artisan;

import com.artisanmarketplace.artisan_marketplace.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "artisan_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtisanProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    // Location fields - will be used in Step 6 with PostGIS
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_name")
    private String locationName; // e.g., "Cape Town CBD"

    @Column(name = "avg_rating")
    private Double avgRating = 0.0;

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

    // Convenience method to check if profile is complete enough for matching
    public boolean isComplete() {
        return bio != null && hourlyRate != null && latitude != null && longitude != null;
    }

    // Calculate portfolio completeness score (will be used in Step 6)
    public double getPortfolioCompletenessScore() {
        int score = 0;
        if (bio != null && !bio.isBlank()) score += 1;
        if (hourlyRate != null && hourlyRate > 0) score += 1;
        if (locationName != null && !locationName.isBlank()) score += 1;
        if (verified) score += 1;
        // Portfolio media and qualifications will be added in Step 7
        return Math.min(1.0, score / 4.0); // Normalize to 0-1
    }
}