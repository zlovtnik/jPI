package com.churchapp.security

import com.churchapp.entity.User
import com.churchapp.entity.enums.RoleType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.GrantedAuthority
import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockitoExtension::class)
class ChurchUserPrincipalTest {

    private lateinit var userPrincipal: ChurchUserPrincipal
    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .password("password")
            .email("test@example.com")
            .role(RoleType.MEMBER)
            .enabled(true)
            .build()
        
        userPrincipal = ChurchUserPrincipal(testUser)
    }

    @Test
    fun `getAuthorities should return role authority`() {
        // When
        val authorities = userPrincipal.authorities
        
        // Then
        assertEquals(1, authorities.size)
        assertTrue(authorities.any { it.authority == "ROLE_MEMBER" })
    }

    @Test
    fun `getUsername should return correct username`() {
        // When & Then
        assertEquals("testuser", userPrincipal.username)
    }

    @Test
    fun `getPassword should return correct password`() {
        // When & Then
        assertEquals("password", userPrincipal.password)
    }

    @Test
    fun `isEnabled should return user active status`() {
        // When & Then
        assertTrue(userPrincipal.isEnabled)
        
        // Test with inactive user
        testUser.setActive(false)
        assertFalse(userPrincipal.isEnabled)
    }

    @Test
    fun `account status should always return true`() {
        // When & Then
        assertTrue(userPrincipal.isAccountNonExpired)
        assertTrue(userPrincipal.isAccountNonLocked)
        assertTrue(userPrincipal.isCredentialsNonExpired)
    }

    @Test
    fun `getUser should return original user`() {
        // When & Then
        assertEquals(testUser, userPrincipal.getUser())
    }
}
