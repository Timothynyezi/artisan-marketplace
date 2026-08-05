package com.artisanmarketplace.artisan_marketplace.artisan.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ArtisanProfileResponse {
    private UUID Id;
    private UUID userId;
    private String userFullName;
    private String userEmail;
    private String bio;
    private String hourlyRate;
    private Double hourlyRates;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private Double avgRating;
    private Boolean verified;
    private Boolean isActive;
    private Double portfolioCompleteness;
    private boolean isComplete;
}
