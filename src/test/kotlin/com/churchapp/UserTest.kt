package com.churchapp

import com.churchapp.entity.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("User Entity Tests")
class UserTest {
    private lateinit var user: User

    @BeforeEach
    fun setup() {
        user =
            User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build()
    }

    @Test
    fun `should create user with builder pattern`() {
        assertNotNull(user)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals("password123", user.password)
        assertTrue(user.isEnabled)
    }

    @Test
    fun `should have proper UserDetails implementation`() {
        assertTrue(user.isAccountNonExpired)
        assertTrue(user.isAccountNonLocked)
        assertTrue(user.isCredentialsNonExpired)
        assertTrue(user.isEnabled)
    }

    @Test
    fun `should handle Option types correctly`() {
        assertTrue(user.getIdOption().isEmpty()) // No ID until persisted
        assertTrue(user.getUpdatedAtOption().isEmpty()) // No update time initially
    }

    @Test
    fun `should allow setting active status`() {
        user.setActive(false)
        assertFalse(user.isEnabled)

        user.setActive(true)
        assertTrue(user.isEnabled)
    }
}
