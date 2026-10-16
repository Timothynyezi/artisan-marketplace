package com.artisanmarketplace.artisan_marketplace.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long-for-hs256";
    private static final long ACCESS_EXPIRY_MS = 900_000L;     // 15 minutes
    private static final long REFRESH_EXPIRY_MS = 604_800_000L; // 7 days

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);
    }

    @Test
    void shouldIssueTokenThatRoundTripsUserIdAndRole() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "CLIENT");

        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractRole(token)).isEqualTo("CLIENT");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void shouldRejectExpiredToken() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1L, 1L);
        UUID userId = UUID.randomUUID();

        String token = shortLived.generateAccessToken(userId, "CLIENT");
        Thread.sleep(10);  // let the 1ms expiry pass

        assertThat(shortLived.isTokenValid(token)).isFalse();
    }

    @Test
    void shouldRejectTamperedToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, "CLIENT");

        // Flip the last character of the signature
        char lastChar = token.charAt(token.length() - 1);
        char flipped = (lastChar == 'A') ? 'B' : 'A';
        String tampered = token.substring(0, token.length() - 1) + flipped;

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    void shouldRejectTokenSignedWithDifferentKey() {
        JwtService otherService = new JwtService(
                "a-completely-different-secret-also-at-least-32-bytes",
                ACCESS_EXPIRY_MS,
                REFRESH_EXPIRY_MS
        );
        UUID userId = UUID.randomUUID();

        String token = otherService.generateAccessToken(userId, "ARTISAN");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void shouldGenerateDistinctTokensForAccessAndRefresh() {
        UUID userId = UUID.randomUUID();

        String access = jwtService.generateAccessToken(userId, "CLIENT");
        String refresh = jwtService.generateRefreshToken(userId, "CLIENT");

        assertThat(access).isNotEqualTo(refresh);
    }
}