package com.artisanmarketplace.artisan_marketplace.auth.dto;

import com.artisanmarketplace.artisan_marketplace.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    @jakarta.validation.constraints.NotNull(message = "Role is required")
    private Role role;

}
