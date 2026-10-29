package com.artisanmarketplace.artisan_marketplace.job;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.artisanmarketplace.artisan_marketplace.job.dto.JobResponseDto;
import com.artisanmarketplace.artisan_marketplace.security.JwtService;

@WebMvcTest(controllers = JobResponseController.class)
@AutoConfigureMockMvc
@Import(JobResponseControllerTest.MethodSecurityConfig.class)
class JobResponseControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobResponseService jobResponseService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CLIENT")
    void shouldRejectSubmitProposalWhenClient() throws Exception {

        UUID jobId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/jobs/" + jobId + "/responds")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000002", roles = "ARTISAN")
    void shouldAllowSubmitProposalWhenArtisan() throws Exception {
        UUID jobId = UUID.randomUUID();
        JobResponseDto response = new JobResponseDto(UUID.randomUUID(), jobId, UUID.randomUUID(), "Artisan Bob", "I can do it", 500.0, JobResponseStatus.PENDING);
        
        when(jobResponseService.submitProposal(eq(jobId), any(UUID.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/jobs/" + jobId + "/responds")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.artisanName").value("Artisan Bob"));
    }

    private String sampleRequestJson() {
        return """
                {
                  "message": "I can do it",
                  "proposedRate": 500.0
                }
                """;
    }
}