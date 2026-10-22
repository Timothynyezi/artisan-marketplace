package com.artisanmarketplace.artisan_marketplace.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JobStatusTest {

    // The canonical list of valid transitions from the design doc.
    // If you ever change JobStatus, this map must change too — and this test will tell you.
    private static final Map<JobStatus, Set<JobStatus>> VALID_TRANSITIONS = Map.of(
            JobStatus.OPEN, Set.of(JobStatus.REQUESTED, JobStatus.CANCELLED),
            JobStatus.REQUESTED, Set.of(JobStatus.ACCEPTED, JobStatus.OPEN, JobStatus.CANCELLED),
            JobStatus.ACCEPTED, Set.of(JobStatus.IN_PROGRESS, JobStatus.CANCELLED),
            JobStatus.IN_PROGRESS, Set.of(JobStatus.AWAITING_CONFIRMATION),
            JobStatus.AWAITING_CONFIRMATION, Set.of(JobStatus.COMPLETED),
            JobStatus.COMPLETED, Set.of(),
            JobStatus.CANCELLED, Set.of()
    );

    @Test
    void everyStateHasAnEntryInTheTransitionMap() {
        // Guards against a new state being added without updating the map
        for (JobStatus from : JobStatus.values()) {
            assertThat(VALID_TRANSITIONS).containsKey(from);
        }
    }

    @Test
    void everyValidTransitionIsAllowed() {
        VALID_TRANSITIONS.forEach((from, targets) ->
                targets.forEach(to ->
                        assertThat(from.canTransitionTo(to))
                                .as("%s -> %s should be allowed", from, to)
                                .isTrue()));
    }

    @Test
    void everyInvalidTransitionIsRejected() {
        for (JobStatus from : JobStatus.values()) {
            Set<JobStatus> allowed = VALID_TRANSITIONS.getOrDefault(from, Set.of());
            for (JobStatus to : JobStatus.values()) {
                if (!allowed.contains(to)) {
                    assertThat(from.canTransitionTo(to))
                            .as("%s -> %s should be rejected", from, to)
                            .isFalse();
                }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(JobStatus.class)
    void noStateCanTransitionToItself(JobStatus state) {
        // "In-place" transitions are meaningless and should always be false
        assertThat(state.canTransitionTo(state)).isFalse();
    }

    @Test
    void completedIsTerminal() {
        for (JobStatus to : JobStatus.values()) {
            assertThat(JobStatus.COMPLETED.canTransitionTo(to)).isFalse();
        }
    }

    @Test
    void cancelledIsTerminal() {
        for (JobStatus to : JobStatus.values()) {
            assertThat(JobStatus.CANCELLED.canTransitionTo(to)).isFalse();
        }
    }

    @Test
    void cannotSkipFromOpenToCompleted() {
        // Design doc's specific call-out: "a job cannot skip states (e.g. OPEN directly to COMPLETED)"
        assertThat(JobStatus.OPEN.canTransitionTo(JobStatus.COMPLETED)).isFalse();
    }

    @Test
    void cannotSkipFromAcceptedToAwaitingConfirmation() {
        // Work must go ACCEPTED -> IN_PROGRESS -> AWAITING_CONFIRMATION
        assertThat(JobStatus.ACCEPTED.canTransitionTo(JobStatus.AWAITING_CONFIRMATION)).isFalse();
    }

    @Test
    void artisanCannotMarkCompletedDirectly() {
        // Design doc's trust-gap fix: only the client confirms completion
        assertThat(JobStatus.IN_PROGRESS.canTransitionTo(JobStatus.COMPLETED)).isFalse();
        assertThat(JobStatus.AWAITING_CONFIRMATION.canTransitionTo(JobStatus.COMPLETED)).isTrue();
    }

    @Test
    void requestCanBeDeclinedBackToOpen() {
        // Design doc: "REQUESTED -> OPEN, triggered by Artisan, origin = OPEN_POST"
        assertThat(JobStatus.REQUESTED.canTransitionTo(JobStatus.OPEN)).isTrue();
    }
}