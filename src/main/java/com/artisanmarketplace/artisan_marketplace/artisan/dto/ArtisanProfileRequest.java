package com.artisanmarketplace.artisan_marketplace.artisan.dto;

import lombok.Data;

@Data
public class ArtisanProfileRequest {
    private String bio;
    private Double hourlyRate;
    private Double latitude;
    private Double longitude;
    private Double locationName;
}
