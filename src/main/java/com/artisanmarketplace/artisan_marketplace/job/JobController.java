package com.artisanmarketplace.artisan_marketplace.job;

import com.artisanmarketplace.artisan_marketplace.job.dto.JobCreateRequest;
import com.artisanmarketplace.artisan_marketplace.job.dto.JobSummaryResponse;
import com.artisanmarketplace.artisan_marketplace.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')") // <-- Added here
    public ResponseEntity<JobSummaryResponse> createJob(@Valid @RequestBody JobCreateRequest request) {
        UUID clientId = SecurityUtils.getCurrentUserId();
        
        Job job = jobService.createJob(
                clientId,
                request.getTradeRequired(),
                request.getTitle(),
                request.getDescription(),
                request.getLatitude(),
                request.getLongitude(),
                request.getLocationName(),
                request.getOrigin(),
                request.getBudget()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(job));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> transitionStatus(
            @PathVariable UUID id,
            @RequestParam JobStatus targetStatus) {
        
        UUID userId = SecurityUtils.getCurrentUserId();
        jobService.transitionStatus(id, userId, targetStatus);
        
        return ResponseEntity.noContent().build();
    }

    private JobSummaryResponse mapToResponse(Job job) {
        return new JobSummaryResponse(
                job.getId(),
                job.getTitle(),
                job.getStatus(),
                job.getOrigin(),
                job.getBudget()
        );
    }
}