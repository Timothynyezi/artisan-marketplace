package com.artisanmarketplace.artisan_marketplace.job;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JobResponseRepository extends JpaRepository<JobResponse, UUID> {
    List<JobResponse> findByJobId(UUID jobId);
    boolean existsByjobIdAndArtisanId(UUID jobId, UUID artisanId);
}
