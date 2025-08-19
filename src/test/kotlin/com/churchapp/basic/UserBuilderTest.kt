package com.churchapp.basic

import com.churchapp.entity.User
import com.churchapp.entity.enums.RoleType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class UserBuilderTest {
    @Test
    fun testBasicUserCreation() {
        val fixedId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val user =
            User.builder()
                .id(fixedId)
                .username("testuser")
                .password("dummy-password-123!")
                .email("test@example.com")
                .role(RoleType.MEMBER)
                .enabled(true)
                .build()

        assertAll(
            { assertNotNull(user) },
            { assertEquals(fixedId, user.id) },
            { assertEquals("testuser", user.username) },
            { assertEquals("test@example.com", user.email) },
            { assertEquals(RoleType.MEMBER, user.role) },
            { assertTrue(user.isEnabled) },
        )
    }
}
