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
}
