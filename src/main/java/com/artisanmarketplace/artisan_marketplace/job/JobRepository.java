package com.artisanmarketplace.artisan_marketplace.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
    List<Job> findByClientId(UUID clientId);
    List<Job> findByAssignedArtisanId(UUID artisanId);
}
