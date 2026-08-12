package com.artisanmarketplace.artisan_marketplace.artisan;

import com.artisanmarketplace.artisan_marketplace.artisan.dto.ArtisanProfileRequest;
import com.artisanmarketplace.artisan_marketplace.artisan.dto.ArtisanProfileResponse;
import com.artisanmarketplace.artisan_marketplace.user.Role;
import com.artisanmarketplace.artisan_marketplace.user.User;
import com.artisanmarketplace.artisan_marketplace.user.UserRepository;
import com.artisanmarketplace.artisan_marketplace.common.exception.AccessDeniedException;
import com.artisanmarketplace.artisan_marketplace.common.exception.ResourceNotFoundException;
import com.artisanmarketplace.artisan_marketplace.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtisanProfileService {

    private final ArtisanProfileRepository artisanProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ArtisanProfileResponse createProfile(UUID userId, ArtisanProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.ARTISAN) {
            throw new ValidationException("Only users with ARTISAN role can create a profile");
        }

        if (artisanProfileRepository.existsByUserId(userId)) {
            throw new ValidationException("Artisan profile already exists for this user");
        }

        ArtisanProfile profile = new ArtisanProfile(
                user,
                request.getBio(),
                request.getHourlyRate(),
                request.getLatitude(),
                request.getLongitude(),
                request.getLocationName()
        );

        ArtisanProfile saved = artisanProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ArtisanProfileResponse getProfile(UUID profileId) {
        ArtisanProfile profile = findProfileOrThrow(profileId);
        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public ArtisanProfileResponse getProfileByUserId(UUID userId) {
        ArtisanProfile profile = artisanProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Artisan profile not found for user"));
        return mapToResponse(profile);
    }

    @Transactional
    public ArtisanProfileResponse updateProfile(UUID profileId, UUID requestingUserId, ArtisanProfileRequest request) {
        ArtisanProfile profile = findProfileOrThrow(profileId);

        if (!profile.getUser().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        profile.updateProfile(
                request.getBio(),
                request.getHourlyRate(),
                request.getLatitude(),
                request.getLongitude(),
                request.getLocationName()
        );

        ArtisanProfile updated = artisanProfileRepository.save(profile);
        return mapToResponse(updated);
    }

    @Transactional
    public void toggleActiveStatus(UUID profileId, UUID requestingUserId) {
        ArtisanProfile profile = findProfileOrThrow(profileId);

        if (!profile.getUser().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("You can only modify your own profile");
        }

        profile.toggleActive();
        artisanProfileRepository.save(profile);
    }

    private ArtisanProfile findProfileOrThrow(UUID profileId) {
        return artisanProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Artisan profile not found with id: " + profileId));
    }

    private ArtisanProfileResponse mapToResponse(ArtisanProfile profile) {
        ArtisanProfileResponse response = new ArtisanProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setUserFullName(profile.getUser().getFullName());
        response.setUserEmail(profile.getUser().getEmail());
        response.setBio(profile.getBio());
        response.setHourlyRate(profile.getHourlyRate());
        response.setLatitude(profile.getLatitude());
        response.setLongitude(profile.getLongitude());
        response.setLocationName(profile.getLocationName());
        response.setAvgRating(profile.getAvgRating());
        response.setVerified(profile.getVerified());
        response.setIsActive(profile.getIsActive());
        response.setPortfolioCompleteness(profile.getPortfolioCompletenessScore());
        response.setComplete(profile.isComplete());
        return response;
    }
}