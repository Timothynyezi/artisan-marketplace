package com.artisanmarketplace.artisan_marketplace.job;

import com.artisanmarketplace.artisan_marketplace.job.dto.JobResponseDto;
import com.artisanmarketplace.artisan_marketplace.job.dto.JobResponseRequest;
import com.artisanmarketplace.artisan_marketplace.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobResponseController {

    private final JobResponseService jobResponseService;

    @PostMapping("/{jobId}/responds")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<JobResponseDto> submitProposal(
            @PathVariable UUID jobId,
            @Valid @RequestBody JobResponseRequest request) {
        
        UUID artisanId = SecurityUtils.getCurrentUserId();
        JobResponseDto response = jobResponseService.submitProposal(jobId, artisanId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{jobId}/responds")
    public ResponseEntity<List<JobResponseDto>> getProposals(@PathVariable UUID jobId) {
        UUID clientId = SecurityUtils.getCurrentUserId();
        List<JobResponseDto> proposals = jobResponseService.getProposalsForJob(jobId, clientId);
        return ResponseEntity.ok(proposals);
    }

    @PatchMapping("/{jobId}/responds/{proposalId}/accept")
    public ResponseEntity<Void> acceptProposal(
            @PathVariable UUID jobId,
            @PathVariable UUID proposalId) {
        
        UUID clientId = SecurityUtils.getCurrentUserId();
        jobResponseService.acceptProposal(jobId, proposalId, clientId);
        return ResponseEntity.ok().build();
    }
}