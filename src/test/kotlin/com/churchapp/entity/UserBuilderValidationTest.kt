package com.churchapp.entity

import com.churchapp.entity.enums.RoleType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class UserBuilderValidationTest {

    @Test
    fun `should build user successfully with all required fields`() {
        val user = User.builder()
            .username("testuser")
            .password("password123")
            .email("test@example.com")
            .build()

        assertNotNull(user)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals(RoleType.MEMBER, user.role)
        assertTrue(user.isEnabled)
        assertNotNull(user.createdAt)
    }

    @Test
    fun `should throw IllegalStateException when username is missing`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            User.builder()
                .password("password123")
                .email("test@example.com")
                .build()
        }
        assertTrue(exception.message!!.contains("username"))
    }

    @Test
    fun `should throw IllegalStateException when password is missing`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            User.builder()
                .username("testuser")
                .email("test@example.com")
                .build()
        }
        assertTrue(exception.message!!.contains("password"))
    }

    @Test
    fun `should throw IllegalStateException when email is missing`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            User.builder()
                .username("testuser")
                .password("password123")
                .build()
        }
        assertTrue(exception.message!!.contains("email"))
    }

    @Test
    fun `should throw IllegalStateException when multiple fields are missing`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            User.builder()
                .username("testuser")
                .build()
        }
        assertTrue(exception.message!!.contains("password"))
        assertTrue(exception.message!!.contains("email"))
    }

    @Test
    fun `should throw IllegalStateException when username is blank`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            User.builder()
                .username("   ")
                .password("password123")
                .email("test@example.com")
                .build()
        }
        assertTrue(exception.message!!.contains("username"))
    }

    @Test
    fun `should use provided createdAt when specified`() {
        val specificTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        val user = User.builder()
            .username("testuser")
            .password("password123")
            .email("test@example.com")
            .createdAt(specificTime)
            .build()

        assertEquals(specificTime, user.createdAt)
    }

    @Test
    fun `should set createdAt to now when not specified`() {
        val beforeBuild = LocalDateTime.now()
        val user = User.builder()
            .username("testuser")
            .password("password123")
            .email("test@example.com")
            .build()
        val afterBuild = LocalDateTime.now()

        assertTrue(user.createdAt.isAfter(beforeBuild.minusSeconds(1)))
        assertTrue(user.createdAt.isBefore(afterBuild.plusSeconds(1)))
    }
}
