package com.artisanmarketplace.artisan_marketplace.job;

import com.artisanmarketplace.artisan_marketplace.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_responses")
@Getter
public class JobResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artisan_id", nullable = false)
    private User artisan;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "proposed_rate", nullable = false)
    private Double proposedRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobResponseStatus status = JobResponseStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    protected JobResponse() {
        // required by JPA
    }

    public JobResponse(Job job, User artisan, String message, Double proposedRate) {
        this.job = job;
        this.artisan = artisan;
        this.message = message;
        this.proposedRate = proposedRate;
        this.status = JobResponseStatus.PENDING;
    }

    // Intentional, controlled mutation
    public void markAsAccepted() {
        this.status = JobResponseStatus.ACCEPTED;
    }

    public void markAsRejected() {
        this.status = JobResponseStatus.REJECTED;
    }
}