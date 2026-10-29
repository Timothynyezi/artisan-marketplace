package com.artisanmarketplace.artisan_marketplace.job;

import com.artisanmarketplace.artisan_marketplace.common.exception.AccessDeniedException;
import com.artisanmarketplace.artisan_marketplace.common.exception.ResourceNotFoundException;
import com.artisanmarketplace.artisan_marketplace.common.exception.ValidationException;
import com.artisanmarketplace.artisan_marketplace.job.dto.JobResponseDto;
import com.artisanmarketplace.artisan_marketplace.job.dto.JobResponseRequest;
import com.artisanmarketplace.artisan_marketplace.user.Role;
import com.artisanmarketplace.artisan_marketplace.user.User;
import com.artisanmarketplace.artisan_marketplace.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JobResponseService {

    private final JobRepository jobRepository;
    private final JobResponseRepository jobResponseRepository;
    private final UserRepository userRepository;

    @Transactional
    public JobResponseDto submitProposal(UUID jobId, UUID artisanId, JobResponseRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != JobStatus.OPEN || job.getOrigin() != JobOrigin.OPEN_POST) {
            throw new ValidationException("This job is not open for proposals");
        }

        User artisan = userRepository.findById(artisanId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (artisan.getRole() != Role.ARTISAN) {
            throw new ValidationException("Only ARTISAN users can submit proposals");
        }

        if (jobResponseRepository.existsByJobIdAndArtisanId(jobId, artisanId)) {
            throw new ValidationException("You have already submitted a proposal for this job");
        }

        JobResponse response = new JobResponse(job, artisan, request.getMessage(), request.getProposedRate());
        JobResponse saved = jobResponseRepository.save(response);
        
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<JobResponseDto> getProposalsForJob(UUID jobId, UUID requestingUserId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getClient().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("Only the job owner can view proposals");
        }

        return jobResponseRepository.findByJobId(jobId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public void acceptProposal(UUID jobId, UUID proposalId, UUID clientId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getClient().getId().equals(clientId)) {
            throw new AccessDeniedException("Only the job owner can accept proposals");
        }

        JobResponse proposal = jobResponseRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));

        if (!proposal.getJob().getId().equals(jobId)) {
            throw new ValidationException("Proposal does not belong to this job");
        }

        // 1. Accept the chosen proposal
        proposal.markAsAccepted();
        jobResponseRepository.save(proposal);

        // 2. Reject all other proposals for this job
        List<JobResponse> otherProposals = jobResponseRepository.findByJobId(jobId);
        for (JobResponse other : otherProposals) {
            if (!java.util.Objects.equals(other.getId(), proposalId)) {
                other.markAsRejected();
                jobResponseRepository.save(other);
            }
        }

        // 3. Assign the artisan and transition the job
        job.assignArtisan(proposal.getArtisan());
        job.transitionTo(JobStatus.REQUESTED);
        jobRepository.save(job);
    }

    private JobResponseDto mapToDto(JobResponse response) {
        return new JobResponseDto(
                response.getId(),
                response.getJob().getId(),
                response.getArtisan().getId(),
                response.getArtisan().getFullName(),
                response.getMessage(),
                response.getProposedRate(),
                response.getStatus()
        );
    }
}