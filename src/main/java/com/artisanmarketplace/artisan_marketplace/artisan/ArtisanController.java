package com.artisanmarketplace.artisan_marketplace.artisan;

import com.artisanmarketplace.artisan_marketplace.artisan.dto.ArtisanProfileRequest;
import com.artisanmarketplace.artisan_marketplace.artisan.dto.ArtisanProfileResponse;
import com.artisanmarketplace.artisan_marketplace.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/artisans")
@RequiredArgsConstructor
public class ArtisanController {

    private final ArtisanProfileService artisanProfileService;

    // Public: Anyone can view an artisan's profile by their profile ID
    @GetMapping("/{id}")
    public ResponseEntity<ArtisanProfileResponse> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(artisanProfileService.getProfile(id));
    }

    // Private: Artisan creates their own profile
    @PostMapping("/me")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ArtisanProfileResponse> createProfile(@Valid @RequestBody ArtisanProfileRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        ArtisanProfileResponse response = artisanProfileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Private: Artisan updates their own profile
    @PutMapping("/me")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ArtisanProfileResponse> updateMyProfile(@Valid @RequestBody ArtisanProfileRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        
        // The service currently expects (profileId, requestingUserId, request).
        // We can fetch the profile by userId first to get the profileId.
        ArtisanProfileResponse existingProfile = artisanProfileService.getProfileByUserId(userId);
        
        ArtisanProfileResponse response = artisanProfileService.updateProfile(
                existingProfile.getId(), 
                userId, 
                request
        );
        return ResponseEntity.ok(response);
    }

    // Private: Artisan toggles their own active status
    @PatchMapping("/me/status")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<Void> toggleMyStatus() {
        UUID userId = SecurityUtils.getCurrentUserId();
        ArtisanProfileResponse existingProfile = artisanProfileService.getProfileByUserId(userId);
        
        artisanProfileService.toggleActiveStatus(existingProfile.getId(), userId);
        return ResponseEntity.noContent().build();
    }
}
