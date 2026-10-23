package com.sapota.seo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void correctPasswordVerifies() {
        String stored = PasswordHasher.hash("admin123");
        assertTrue(PasswordHasher.verify("admin123", stored));
    }

    @Test
    void wrongPasswordIsRejected() {
        String stored = PasswordHasher.hash("admin123");
        assertFalse(PasswordHasher.verify("admin124", stored));
        assertFalse(PasswordHasher.verify("", stored));
    }

    @Test
    void samePasswordProducesDifferentHashesBecauseOfRandomSalt() {
        assertNotEquals(PasswordHasher.hash("abc123"), PasswordHasher.hash("abc123"));
    }

    @Test
    void storedValueDoesNotContainPlainPassword() {
        assertFalse(PasswordHasher.hash("matkhau-bi-mat").contains("matkhau-bi-mat"));
    }

    @Test
    void malformedStoredValueIsRejectedWithoutException() {
        assertFalse(PasswordHasher.verify("x", "khong-phai-hash"));
        assertFalse(PasswordHasher.verify("x", "pbkdf2$abc$###$###"));
        assertFalse(PasswordHasher.verify("x", null));
        assertFalse(PasswordHasher.verify(null, PasswordHasher.hash("x")));
    }

    @Test
    void unicodePasswordWorks() {
        String stored = PasswordHasher.hash("Mật-khẩu-tiếng-Việt-2026");
        assertTrue(PasswordHasher.verify("Mật-khẩu-tiếng-Việt-2026", stored));
    }
}
