package com.example.project_management_tool.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // 32+ chars minimum pour HS256
    private static final String SECRET =
            "0123456789ABCDEF0123456789ABCDEF"; // 32 chars

    @Test
    void generateToken_and_extractEmail_shouldWork() {
        JwtService jwtService = new JwtService(SECRET, 60_000); // 1 min

        String token = jwtService.generateToken("john@example.com", "ADMIN");
        String email = jwtService.extractEmail(token);

        assertEquals("john@example.com", email);
    }

    @Test
    void extractEmail_shouldThrow_whenTokenIsInvalid() {
        JwtService jwtService = new JwtService(SECRET, 60_000);

        String invalidToken = "not.a.jwt";

        assertThrows(JwtException.class, () -> jwtService.extractEmail(invalidToken));
    }

    @Test
    void extractEmail_shouldThrow_whenTokenIsExpired() throws InterruptedException {
        JwtService jwtService = new JwtService(SECRET, 1); // expire quasi immédiatement

        String token = jwtService.generateToken("john@example.com", "ADMIN");

        Thread.sleep(80); // plus robuste que 5ms (CI)

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractEmail(token));
    }
}
