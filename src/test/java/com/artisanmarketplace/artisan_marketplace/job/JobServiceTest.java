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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TradeRepository tradeRepository;
    @InjectMocks
    private JobService jobService;

    // ---------- createJob ----------

    @Test
    void shouldCreateJobForClient() {
        UUID clientId = UUID.randomUUID();
        User client = new User("client@test.com", "hash", Role.CLIENT, "Client", "123");
        Trade trade = new Trade(TradeName.PLUMBER);

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(tradeRepository.findByName(TradeName.PLUMBER)).thenReturn(Optional.of(trade));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = jobService.createJob(
                clientId, TradeName.PLUMBER, "Fix sink", "Leaking pipe",
                -33.9, 18.4, "Cape Town", JobOrigin.OPEN_POST, 500.0
        );

        assertThat(result.getClient()).isEqualTo(client);
        assertThat(result.getTradeRequired()).isEqualTo(trade);
        assertThat(result.getStatus()).isEqualTo(JobStatus.OPEN);
        assertThat(result.getOrigin()).isEqualTo(JobOrigin.OPEN_POST);
    }

    @Test
    void shouldSetStatusToRequestedForDirectSearchJobs() {
        UUID clientId = UUID.randomUUID();
        User client = new User("client@test.com", "hash", Role.CLIENT, "Client", "123");
        Trade trade = new Trade(TradeName.PLUMBER);

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(tradeRepository.findByName(TradeName.PLUMBER)).thenReturn(Optional.of(trade));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = jobService.createJob(
                clientId, TradeName.PLUMBER, "Fix sink", "Leaking pipe",
                -33.9, 18.4, "Cape Town", JobOrigin.DIRECT_SEARCH, 500.0
        );

        assertThat(result.getStatus()).isEqualTo(JobStatus.REQUESTED);
    }

    @Test
    void shouldRejectJobCreationIfUserIsArtisan() {
        UUID userId = UUID.randomUUID();
        User artisan = new User("artisan@test.com", "hash", Role.ARTISAN, "Artisan", "123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(artisan));

        assertThatThrownBy(() -> jobService.createJob(
                userId, TradeName.PLUMBER, "Fix sink", "desc",
                -33.9, 18.4, "Cape Town", JobOrigin.OPEN_POST, 500.0
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Only CLIENT users can create jobs");

        verify(jobRepository, never()).save(any());
    }

    @Test
    void shouldRejectJobCreationIfTradeDoesNotExist() {
        UUID clientId = UUID.randomUUID();
        User client = new User("client@test.com", "hash", Role.CLIENT, "Client", "123");

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(tradeRepository.findByName(TradeName.PLUMBER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.createJob(
                clientId, TradeName.PLUMBER, "Fix sink", "desc",
                -33.9, 18.4, "Cape Town", JobOrigin.OPEN_POST, 500.0
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Invalid trade");

        verify(jobRepository, never()).save(any());
    }

    // ---------- transitionStatus ----------

    @Test
    void shouldAllowClientToCancelOpenJob() {
        UUID jobId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        User client = new User("client@test.com", "hash", Role.CLIENT, "Client", "123");
        // Using reflection or a setter to set the ID for the test
        // (assuming your User has a way to get an ID; if it's null, mocking the ID directly isn't possible)
        
        // For this test we need the job to have a client with the matching ID.
        // Because User's ID is generated by JPA, we need to mock the getter.
        User mockClient = mock(User.class);
        when(mockClient.getId()).thenReturn(clientId);

        Trade trade = new Trade(TradeName.PLUMBER);
        Job job = new Job(mockClient, trade, "title", "desc", 0.0, 0.0, "loc", JobOrigin.OPEN_POST, 100.0);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = jobService.transitionStatus(jobId, clientId, JobStatus.CANCELLED);

        assertThat(result.getStatus()).isEqualTo(JobStatus.CANCELLED);
    }

    @Test
    void shouldRejectStatusChangeIfUserIsNotParticipant() {
        UUID jobId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();

        User mockClient = mock(User.class);
        when(mockClient.getId()).thenReturn(clientId);

        Trade trade = new Trade(TradeName.PLUMBER);
        Job job = new Job(mockClient, trade, "title", "desc", 0.0, 0.0, "loc", JobOrigin.OPEN_POST, 100.0);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.transitionStatus(jobId, strangerId, JobStatus.CANCELLED))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not a participant");
    }

    @Test
    void shouldRejectInvalidTransitionFromStateMachine() {
        UUID jobId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        User mockClient = mock(User.class);
        when(mockClient.getId()).thenReturn(clientId);

        Trade trade = new Trade(TradeName.PLUMBER);
        // Job starts as OPEN
        Job job = new Job(mockClient, trade, "title", "desc", 0.0, 0.0, "loc", JobOrigin.OPEN_POST, 100.0);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        // Try to skip to COMPLETED (invalid)
        assertThatThrownBy(() -> jobService.transitionStatus(jobId, clientId, JobStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid transition");
    }
}
