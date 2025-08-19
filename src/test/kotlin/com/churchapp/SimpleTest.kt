package com.churchapp.entity

import com.churchapp.entity.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import java.util.*

@DisplayName("User Entity Simple Tests")
class SimpleUserTest {

    private lateinit var user: User

    @BeforeEach
    fun setup() {
        user = User.builder()
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
    fun `should get display name correctly`() {
        assertEquals("testuser", user.username)
    }

    @Test
    fun `should have default active status`() {
        assertTrue(user.isEnabled)
    }

    @Test
    fun `should have proper UserDetails implementation`() {
        assertTrue(user.isAccountNonExpired)
        assertTrue(user.isAccountNonLocked)
        assertTrue(user.isCredentialsNonExpired)
        assertTrue(user.isEnabled)
    }
}
