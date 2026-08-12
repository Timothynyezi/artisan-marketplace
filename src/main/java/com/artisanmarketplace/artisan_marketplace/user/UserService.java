package com.artisanmarketplace.artisan_marketplace.user;

import com.artisanmarketplace.artisan_marketplace.auth.dto.RegisterRequest;
import com.artisanmarketplace.artisan_marketplace.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("An account with this email already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = new User(
                request.getEmail(),
                hashedPassword,
                request.getRole(),
                request.getFullName(),
                request.getPhone()
        );

        return userRepository.save(newUser);
    }

    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ValidationException("Invalid email or password");
        }

        return user;
    }
}