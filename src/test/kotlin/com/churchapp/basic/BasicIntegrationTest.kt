package com.churchapp.basic

import com.churchapp.entity.User
import com.churchapp.entity.enums.RoleType
import com.churchapp.security.ChurchUserPrincipal
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class BasicIntegrationTest {

    @Test
    fun testBasicUserCreation() {
        // Test that we can create a User using the builder
        val user = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .password("password")
            .email("test@example.com")
            .role(RoleType.MEMBER)
            .enabled(true)
            .build()

        assertNotNull(user)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals(RoleType.MEMBER, user.role)
        assertTrue(user.isEnabled)
    }
}
