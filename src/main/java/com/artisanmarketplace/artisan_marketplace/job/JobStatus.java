package com.artisanmarketplace.artisan_marketplace.job;

import java.util.Map;
import java.util.Set;

public enum JobStatus {
    OPEN,
    REQUESTED,
    ACCEPTED,
    IN_PROGRESS,
    AWAITING_CONFIRMATION,
    COMPLETED,
    CANCELLED;

    private static final Map<JobStatus, Set<JobStatus>> ALLOWED_TRANSITIONS = Map.of(
            OPEN, Set.of(REQUESTED, CANCELLED),
            REQUESTED, Set.of(ACCEPTED, OPEN, CANCELLED),
            ACCEPTED, Set.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, Set.of(AWAITING_CONFIRMATION),
            AWAITING_CONFIRMATION, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    public boolean canTransitionTo(JobStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}