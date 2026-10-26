package com.artisanmarketplace.artisan_marketplace.job.dto;

import com.artisanmarketplace.artisan_marketplace.job.JobResponseStatus;

import java.util.UUID;

public record JobResponseDto(
        UUID id,
        UUID jobId,
        UUID artisanId,
        String artisanName,
        String message,
        Double proposedRate,
        JobResponseStatus status
) {}