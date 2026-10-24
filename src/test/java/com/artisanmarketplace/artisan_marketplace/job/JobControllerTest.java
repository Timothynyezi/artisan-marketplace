package com.artisanmarketplace.artisan_marketplace.job;

import com.artisanmarketplace.artisan_marketplace.job.dto.JobSummaryResponse;
import com.artisanmarketplace.artisan_marketplace.security.JwtService;
import com.artisanmarketplace.artisan_marketplace.trade.TradeName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JobController.class)
@AutoConfigureMockMvc
@Import(JobControllerTest.MethodSecurityConfig.class)
class JobControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private JwtService jwtService; // Required for the security filter chain to load

    // ---------- POST /api/v1/jobs ----------

    @Test
    void shouldRejectCreateJobWhenAnonymous() throws Exception {
        mockMvc.perform(post("/api/v1/jobs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000002", roles = "ARTISAN")
    void shouldRejectCreateJobWhenArtisan() throws Exception {
        mockMvc.perform(post("/api/v1/jobs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CLIENT")
    void shouldAllowCreateJobWhenClient() throws Exception {
        Job mockJob = mock(Job.class);
        when(mockJob.getId()).thenReturn(UUID.randomUUID());
        when(mockJob.getTitle()).thenReturn("Fix sink");
        when(mockJob.getStatus()).thenReturn(JobStatus.OPEN);
        when(mockJob.getOrigin()).thenReturn(JobOrigin.OPEN_POST);
        when(mockJob.getBudget()).thenReturn(500.0);

        when(jobService.createJob(any(UUID.class), eq(TradeName.PLUMBER), any(), any(), any(), any(), any(), eq(JobOrigin.OPEN_POST), any()))
                .thenReturn(mockJob);

        mockMvc.perform(post("/api/v1/jobs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Fix sink"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    // ---------- PATCH /api/v1/jobs/{id}/status ----------

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CLIENT")
    void shouldAllowStatusTransition() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobService.transitionStatus(eq(jobId), any(UUID.class), eq(JobStatus.CANCELLED)))
                .thenReturn(mock(Job.class));

        mockMvc.perform(patch("/api/v1/jobs/{id}/status?targetStatus=CANCELLED", jobId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ---------- helpers ----------

    private String sampleRequestJson() {
        return """
                {
                  "tradeRequired": "PLUMBER",
                  "title": "Fix sink",
                  "description": "Leaking pipe",
                  "latitude": -33.9249,
                  "longitude": 18.4241,
                  "locationName": "Cape Town CBD",
                  "origin": "OPEN_POST",
                  "budget": 500.0
                }
                """;
    }
}