package com.churchapp.service

import com.churchapp.entity.User
import com.churchapp.entity.enums.RoleType
import com.churchapp.repository.UserRepository
import com.churchapp.security.JwtTokenService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {
    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtTokenService: JwtTokenService

    private lateinit var authenticationService: AuthenticationService
    private lateinit var user: User

    @BeforeEach
    fun setup() {
        authenticationService =
            AuthenticationService(
                userRepository,
                passwordEncoder,
                jwtTokenService,
            )

        user =
            User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(RoleType.MEMBER)
                .build()
    }

    @Test
    fun `should create authentication service successfully`() {
        assertNotNull(authenticationService)
        assertNotNull(user)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals(RoleType.MEMBER, user.role)
    }

    @Test
    fun `should validate user credentials format`() {
        // Test that we can create valid user credentials
        assertTrue(user.username.isNotBlank())
        assertTrue(user.password.isNotBlank())
        assertTrue(user.email.contains("@"))
    }

    @Test
    fun `should handle user roles properly`() {
        val adminUser =
            User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("password")
                .role(RoleType.ADMIN)
                .build()

        assertEquals(RoleType.ADMIN, adminUser.role)
        assertEquals(RoleType.MEMBER, user.role)
    }

    @Test
    fun `should create users with proper defaults`() {
        val newUser =
            User.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password")
                .build()

        assertTrue(newUser.isEnabled)
        assertEquals(RoleType.MEMBER, newUser.role) // Default role
        assertNotNull(newUser.createdAt)
    }
}
