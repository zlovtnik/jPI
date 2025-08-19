package com.churchapp.security;

import com.churchapp.entity.User;
import com.churchapp.entity.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ChurchUserPrincipalTest {

    private ChurchUserPrincipal userPrincipal;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .password("password")
            .email("test@example.com")
            .role(RoleType.MEMBER)
            .enabled(true)
            .build();
        
        userPrincipal = new ChurchUserPrincipal(testUser);
    }

    @Test
    void getAuthorities_ShouldReturnRoleAuthority() {
        // When
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        
        // Then
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_MEMBER")));
    }

    @Test
    void getUsername_ShouldReturnCorrectUsername() {
        // When & Then
        assertEquals("testuser", userPrincipal.getUsername());
    }

    @Test
    void getPassword_ShouldReturnCorrectPassword() {
        // When & Then
        assertEquals("password", userPrincipal.getPassword());
    }

    @Test
    void isEnabled_ShouldReturnUserActiveStatus() {
        // When & Then
        assertTrue(userPrincipal.isEnabled());
        
        // Test with inactive user
        testUser.setActive(false);
        assertFalse(userPrincipal.isEnabled());
    }

    @Test
    void accountStatus_ShouldAlwaysReturnTrue() {
        // When & Then
        assertTrue(userPrincipal.isAccountNonExpired());
        assertTrue(userPrincipal.isAccountNonLocked());
        assertTrue(userPrincipal.isCredentialsNonExpired());
    }

    @Test
    void getUser_ShouldReturnOriginalUser() {
        // When & Then
        assertEquals(testUser, userPrincipal.getUser());
    }
}
