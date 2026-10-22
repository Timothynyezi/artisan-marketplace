package com.artisanmarketplace.artisan_marketplace.job.dto;

import com.artisanmarketplace.artisan_marketplace.job.JobOrigin;
import com.artisanmarketplace.artisan_marketplace.trade.TradeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class JobCreateRequest {
    @NotNull(message = "Trade is required")
    private TradeName tradeRequired;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Double latitude;
    private Double longitude;
    private String locationName;

    @NotNull(message = "Origin is required")
    private JobOrigin origin;

    @NotNull(message = "Budget is required")
    @Positive(message = "Budget must be positive")
    private Double budget;
}
