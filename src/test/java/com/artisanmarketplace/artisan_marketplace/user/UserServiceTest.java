package com.artisanmarketplace.artisan_marketplace.user;

import com.artisanmarketplace.artisan_marketplace.auth.dto.RegisterRequest;
import com.artisanmarketplace.artisan_marketplace.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ---------- registerUser ----------

    @Test
    void shouldRegisterNewUserWithHashedPassword() {
        RegisterRequest request = new RegisterRequest(
                "new@test.com", "plaintext123", Role.CLIENT, "New User", "0821111111"
        );
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintext123")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(request);

        assertThat(saved.getEmail()).isEqualTo("new@test.com");
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$hashed");
        assertThat(saved.getRole()).isEqualTo(Role.CLIENT);
        assertThat(saved.getFullName()).isEqualTo("New User");

        // Verify the plaintext password was never stored
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("plaintext123");
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "taken@test.com", "plaintext123", Role.CLIENT, "Someone", "0821111111"
        );
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");

        // Critically: nothing should be written to the DB
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ---------- authenticate ----------

    @Test
    void shouldAuthenticateWhenPasswordMatches() {
        User user = new User("me@test.com", "$2a$hashed", Role.CLIENT, "Me", "0821111111");
        when(userRepository.findByEmail("me@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plaintext123", "$2a$hashed")).thenReturn(true);

        User result = userService.authenticate("me@test.com", "plaintext123");

        assertThat(result).isSameAs(user);
    }

    @Test
    void shouldRejectAuthenticationWhenPasswordDoesNotMatch() {
        User user = new User("me@test.com", "$2a$hashed", Role.CLIENT, "Me", "0821111111");
        when(userRepository.findByEmail("me@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "$2a$hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticate("me@test.com", "wrongpassword"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void shouldRejectAuthenticationWhenUserDoesNotExist() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate("ghost@test.com", "anything"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email or password");

        // Password verification should never happen if user doesn't exist
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}