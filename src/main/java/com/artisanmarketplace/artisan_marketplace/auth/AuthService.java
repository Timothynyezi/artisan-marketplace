package com.artisanmarketplace.artisan_marketplace.auth;

import com.artisanmarketplace.artisan_marketplace.auth.dto.AuthResponse;
import com.artisanmarketplace.artisan_marketplace.auth.dto.LoginRequest;
import com.artisanmarketplace.artisan_marketplace.auth.dto.RegisterRequest;
import com.artisanmarketplace.artisan_marketplace.auth.dto.UserResponse;
import com.artisanmarketplace.artisan_marketplace.security.JwtService;
import com.artisanmarketplace.artisan_marketplace.user.User;
import com.artisanmarketplace.artisan_marketplace.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        User user = userService.registerUser(request);
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());

        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getRole().name());

        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name()
        );

        return new AuthResponse(accessToken, refreshToken, userResponse);
    }
}