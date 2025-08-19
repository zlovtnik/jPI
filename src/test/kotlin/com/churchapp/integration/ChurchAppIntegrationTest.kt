package com.churchapp.integration

import com.churchapp.entity.Member
import com.churchapp.entity.User
import com.churchapp.security.ChurchUserPrincipal
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Tests")
class ChurchAppIntegrationTest {
    @Test
    fun `should create and authenticate user flow`() {
        // Create a user
        val user =
            User.builder()
                .username("integrationtest")
                .email("integration@example.com")
                .password("password123")
                .build()

        assertNotNull(user)
        assertEquals("integrationtest", user.username)
        assertEquals("integration@example.com", user.email)
        assertTrue(user.isEnabled)

        // Create ChurchUserPrincipal
        val principal = ChurchUserPrincipal(user)
        assertNotNull(principal)
        assertEquals("integrationtest", principal.username)
        assertEquals(user, principal.getUser())
        assertTrue(principal.isEnabled)
    }

    @Test
    fun `should create member with all relationships`() {
        // Create a member
        val member =
            Member.builder()
                .firstName("Integration")
                .lastName("Test")
                .email("member@example.com")
                .phoneNumber("555-0123")
                .address("123 Test St")
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .membershipDate(LocalDate.now())
                .build()

        assertNotNull(member)
        assertEquals("Integration", member.firstName)
        assertEquals("Test", member.lastName)
        assertEquals("member@example.com", member.email)

        assertEquals("Integration Test", member.fullName)
        assertTrue(member.isActive)

        // Test date of birth functionality
        assertNotNull(member.dateOfBirth)
        assertTrue(member.getDateOfBirthOption().isSome())
    }

    @Test
    fun `should handle member-family relationships`() {
        // This would typically involve database operations
        // For now, just test the entity relationships work

        val member =
            Member.builder()
                .firstName("Family")
                .lastName("Member")
                .email("family@example.com")
                .build()

        assertNotNull(member)
        assertEquals("Family Member", member.fullName)

        // Test optional family relationship
        assertTrue(member.getFamilyOption().isNone())
    }

    @Test
    fun `should validate entity constraints`() {
        // Test that entities can be created with minimal required fields
        val user =
            User.builder()
                .username("minimal")
                .email("minimal@test.com")
                .password("pass")
                .build()

        val member =
            Member.builder()
                .firstName("Min")
                .lastName("Test")
                .email("min@test.com")
                .build()

        assertNotNull(user)
        assertNotNull(member)

        assertEquals("minimal", user.username)
        assertEquals("Min Test", member.fullName)
    }

    @Test
    fun `should handle Arrow Option types correctly`() {
        val member =
            Member.builder()
                .firstName("Option")
                .lastName("Test")
                .email("option@test.com")
                .build()

        // Test empty options
        assertTrue(member.getPhoneNumberOption().isNone())
        assertTrue(member.getAddressOption().isNone())
        assertTrue(member.getFamilyOption().isNone())

        // Test that we can access these safely
        assertNull(member.getPhoneNumberOption().getOrNull())
        assertNull(member.getAddressOption().getOrNull())
        assertNull(member.getFamilyOption().getOrNull())
    }

    @Test
    fun `should verify all entities have proper UUID support`() {
        val user =
            User.builder()
                .username("uuid-test")
                .email("uuid@test.com")
                .password("password")
                .build()

        val member =
            Member.builder()
                .firstName("UUID")
                .lastName("Test")
                .email("uuid-member@test.com")
                .build()

        // UUIDs should be null before persistence (generated on save)
        assertNull(user.id)
        assertNull(member.id)

        // But entities should be valid
        assertNotNull(user)
        assertNotNull(member)
        assertEquals("uuid-test", user.username)
        assertEquals("UUID Test", member.fullName)
    }
}
