package com.artisanmarketplace.artisan_marketplace.job.dto;

import com.artisanmarketplace.artisan_marketplace.job.JobOrigin;
import com.artisanmarketplace.artisan_marketplace.job.JobStatus;

import java.util.UUID;

public record JobResponse(
        UUID id,
        String title,
        JobStatus status,
        JobOrigin origin,
        Double budget
) {}
