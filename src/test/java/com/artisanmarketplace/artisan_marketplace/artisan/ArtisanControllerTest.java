package com.artisanmarketplace.artisan_marketplace.artisan;

import com.artisanmarketplace.artisan_marketplace.artisan.dto.ArtisanProfileResponse;
import com.artisanmarketplace.artisan_marketplace.security.JwtService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArtisanController.class)
@AutoConfigureMockMvc
@Import(ArtisanControllerTest.MethodSecurityConfig.class)
class ArtisanControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtisanProfileService artisanProfileService;

    @MockitoBean
    private JwtService jwtService;

    // ---------- GET /api/v1/artisans/{id} — public ----------

    @Test
    void shouldAllowAnonymousAccessToGetProfile() throws Exception {
        UUID profileId = UUID.randomUUID();
        ArtisanProfileResponse response = sampleResponse(profileId);
        when(artisanProfileService.getProfile(profileId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/artisans/{id}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId.toString()))
                .andExpect(jsonPath("$.bio").value("Experienced plumber"));
    }

    // ---------- POST /api/v1/artisans/me — ARTISAN only ----------

    @Test
    void shouldRejectCreateProfileWhenAnonymous() throws Exception {
        mockMvc.perform(post("/api/v1/artisans/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CLIENT")
    void shouldRejectCreateProfileWhenClient() throws Exception {
        mockMvc.perform(post("/api/v1/artisans/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000002", roles = "ARTISAN")
    void shouldAllowCreateProfileWhenArtisan() throws Exception {
        ArtisanProfileResponse response = sampleResponse(UUID.randomUUID());
        when(artisanProfileService.createProfile(any(UUID.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/artisans/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId().toString()));
    }

    // ---------- PUT /api/v1/artisans/me — ARTISAN only ----------

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000002", roles = "ARTISAN")
    void shouldAllowUpdateWhenArtisan() throws Exception {
        UUID profileId = UUID.randomUUID();
        ArtisanProfileResponse existing = sampleResponse(profileId);
        ArtisanProfileResponse updated = sampleResponse(profileId);
        updated.setBio("Updated bio");

        when(artisanProfileService.getProfileByUserId(any(UUID.class))).thenReturn(existing);
        when(artisanProfileService.updateProfile(any(UUID.class), any(UUID.class), any()))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/artisans/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Updated bio"));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CLIENT")
    void shouldRejectUpdateWhenClient() throws Exception {
        mockMvc.perform(put("/api/v1/artisans/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson()))
                .andExpect(status().isForbidden());
    }

    // ---------- helpers ----------

    private String sampleRequestJson() {
        return """
                {
                  "bio": "Experienced plumber",
                  "hourlyRate": 250.0,
                  "latitude": -33.9249,
                  "longitude": 18.4241,
                  "locationName": "Cape Town CBD"
                }
                """;
    }

    private ArtisanProfileResponse sampleResponse(UUID profileId) {
        ArtisanProfileResponse resp = new ArtisanProfileResponse();
        resp.setId(profileId);
        resp.setUserId(UUID.randomUUID());
        resp.setUserFullName("Test Artisan");
        resp.setUserEmail("artisan@test.com");
        resp.setBio("Experienced plumber");
        resp.setHourlyRate(250.0);
        resp.setLatitude(-33.9249);
        resp.setLongitude(18.4241);
        resp.setLocationName("Cape Town CBD");
        resp.setAvgRating(0.0);
        resp.setVerified(false);
        resp.setIsActive(true);
        resp.setPortfolioCompleteness(0.5);
        resp.setComplete(true);
        return resp;
    }
}