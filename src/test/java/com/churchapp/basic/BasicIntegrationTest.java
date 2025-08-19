package com.churchapp.basic;

import com.churchapp.entity.User;
import com.churchapp.entity.enums.RoleType;
import com.churchapp.security.ChurchUserPrincipal;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BasicIntegrationTest {

    @Test
    void testBasicUserCreation() {
        // Test that we can create a User using the builder
        User user = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .password("password")
            .email("test@example.com")
            .role(RoleType.MEMBER)
            .enabled(true)
            .build();

        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(RoleType.MEMBER, user.getRole());
        assertTrue(user.isEnabled());
    }

    @Test
    void testChurchUserPrincipal() {
        // Test that ChurchUserPrincipal works with User
        User user = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .password("password")
            .email("test@example.com")
            .role(RoleType.MEMBER)
            .enabled(true)
            .build();

        ChurchUserPrincipal principal = new ChurchUserPrincipal(user);
        
        assertNotNull(principal);
        assertEquals("testuser", principal.getUsername());
        assertEquals("password", principal.getPassword());
        assertTrue(principal.isEnabled());
        assertEquals(user, principal.getUser());
    }
}
