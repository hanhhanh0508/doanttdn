package com.sapota.seo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRoleTest {

    @Test
    void roleParsingIsCaseInsensitive() {
        assertEquals(User.Role.ADMIN, User.Role.fromString("admin"));
        assertEquals(User.Role.STAFF, User.Role.fromString(" Staff "));
    }

    @Test
    void unknownRoleFallsBackToLeastPrivileged() {
        assertEquals(User.Role.STAFF, User.Role.fromString("superuser"));
        assertEquals(User.Role.STAFF, User.Role.fromString(null));
    }

    @Test
    void isAdminOnlyForAdminRole() {
        assertTrue(new User("a", "h", "A", User.Role.ADMIN).isAdmin());
        assertFalse(new User("b", "h", "B", User.Role.STAFF).isAdmin());
    }

    @Test
    void displayNameFallsBackToUsername() {
        assertEquals("Nguyễn Văn A", new User("a", "h", "Nguyễn Văn A", User.Role.STAFF).getDisplayName());
        assertEquals("a", new User("a", "h", " ", User.Role.STAFF).getDisplayName());
    }
}
