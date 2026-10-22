package com.artisanmarketplace.artisan_marketplace.job;

import com.artisanmarketplace.artisan_marketplace.common.exception.AccessDeniedException;
import com.artisanmarketplace.artisan_marketplace.common.exception.ResourceNotFoundException;
import com.artisanmarketplace.artisan_marketplace.common.exception.ValidationException;
import com.artisanmarketplace.artisan_marketplace.trade.Trade;
import com.artisanmarketplace.artisan_marketplace.trade.TradeName;
import com.artisanmarketplace.artisan_marketplace.trade.TradeRepository;
import com.artisanmarketplace.artisan_marketplace.user.Role;
import com.artisanmarketplace.artisan_marketplace.user.User;
import com.artisanmarketplace.artisan_marketplace.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    public Job createJob(UUID clientId, TradeName tradeRequired, String title, String description,
                         Double latitude, Double longitude, String locationName,
                         JobOrigin origin, Double budget) {
        
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (client.getRole() != Role.CLIENT) {
            throw new ValidationException("Only CLIENT users can create jobs");
        }

        Trade trade = tradeRepository.findByName(tradeRequired)
                .orElseThrow(() -> new ValidationException("Invalid trade required"));

        Job job = new Job(
                client, trade, title, description, 
                latitude, longitude, locationName, 
                origin, budget
        );

        return jobRepository.save(job);
    }

    @Transactional
    public Job transitionStatus(UUID jobId, UUID requestingUserId, JobStatus targetStatus) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // 1. Resource-level authorization (who is allowed to make this change?)
        boolean isClient = job.getClient().getId().equals(requestingUserId);
        boolean isArtisan = job.getAssignedArtisan() != null && job.getAssignedArtisan().getId().equals(requestingUserId);

        if (!isClient && !isArtisan) {
            throw new AccessDeniedException("You are not a participant in this job");
        }

        // 2. Apply the state machine guard clause
        job.transitionTo(targetStatus);
        
        return jobRepository.save(job);
    }
}
