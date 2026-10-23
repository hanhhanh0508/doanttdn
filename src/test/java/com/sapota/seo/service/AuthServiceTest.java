package com.sapota.seo.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthServiceTest {

    @Test
    void shortPasswordIsRejected() {
        assertNotNull(AuthService.validatePassword("12345"));
        assertNotNull(AuthService.validatePassword(""));
        assertNotNull(AuthService.validatePassword(null));
    }

    @Test
    void passwordOfMinimumLengthIsAccepted() {
        assertNull(AuthService.validatePassword("123456"));
    }
}
