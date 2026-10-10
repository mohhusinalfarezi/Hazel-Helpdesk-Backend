package com.chatkeluhan.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "defaultSecretKeyWithAtLeast256BitsLong12345678901234567890");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000);
    }

    @Test
    void testGenerateTokenWithDefaultRole() {
        String token = jwtUtils.generateToken("testuser@example.com");
        assertNotNull(token);
        assertEquals("testuser@example.com", jwtUtils.extractUsername(token));
        assertEquals("USER", jwtUtils.extractRole(token));
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void testGenerateTokenWithCustomRole() {
        String techToken = jwtUtils.generateToken("tech@example.com", "TECHNICIAN");
        assertNotNull(techToken);
        assertEquals("tech@example.com", jwtUtils.extractUsername(techToken));
        assertEquals("TECHNICIAN", jwtUtils.extractRole(techToken));

        String adminToken = jwtUtils.generateToken("admin@example.com", "ADMIN");
        assertNotNull(adminToken);
        assertEquals("admin@example.com", jwtUtils.extractUsername(adminToken));
        assertEquals("ADMIN", jwtUtils.extractRole(adminToken));
    }
}
