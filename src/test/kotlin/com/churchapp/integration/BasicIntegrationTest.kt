package com.churchapp.integration

import com.churchapp.dto.MemberDTO
import com.churchapp.entity.Member
import com.churchapp.entity.User
import com.churchapp.entity.enums.RoleType
import com.churchapp.security.ChurchUserPrincipal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

/**
 * Basic integration test to verify that all the core components work together
 * without requiring a full Spring context.
 */
class BasicIntegrationTest {
    @Test
    fun `should create and work with User entity`() {
        // Test User creation and builder pattern
        val user =
            User.builder()
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

        // Test ChurchUserPrincipal with User
        val principal = ChurchUserPrincipal(user)
        assertNotNull(principal)
        assertEquals("testuser", principal.username)
        assertEquals(user, principal.getUser())
        assertTrue(principal.isEnabled)
    }

    @Test
    fun `should create and work with Member entity`() {
        // Test Member creation
        val member =
            Member(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                phoneNumber = "555-1234",
                dateOfBirth = LocalDate.of(1990, 1, 1),
            )

        assertNotNull(member)
        assertEquals("John Doe", member.fullName)
        assertTrue(member.isActive)

        // Test Member builder pattern
        val builtMember =
            Member.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .isActive(false)
                .build()

        assertEquals("Jane", builtMember.firstName)
        assertEquals("Smith", builtMember.lastName)
        assertEquals(false, builtMember.isActive)

        // Test Arrow Option functionality
        val phoneOption = member.getPhoneNumberOption()
        assertTrue(phoneOption.isSome())
        assertEquals("555-1234", phoneOption.getOrNull())

        val addressOption = member.getAddressOption()
        assertTrue(addressOption.isNone())
    }

    @Test
    fun `should create and work with MemberDTO`() {
        // Test MemberDTO with constructor
        val memberDTO = MemberDTO()
        memberDTO.firstName = "Alice"
        memberDTO.lastName = "Johnson"
        memberDTO.email = "alice@example.com"
        memberDTO.familyId = 123

        assertEquals("Alice", memberDTO.firstName)
        assertEquals("Johnson", memberDTO.lastName)
        assertEquals("alice@example.com", memberDTO.email)
        assertEquals(123, memberDTO.familyId)

        // Test MemberDTO with builder
        val builtDTO =
            MemberDTO.builder()
                .firstName("Bob")
                .lastName("Wilson")
                .email("bob@example.com")
                .phone("555-9876")
                .active(false)
                .build()

        assertEquals("Bob", builtDTO.firstName)
        assertEquals("Wilson", builtDTO.lastName)
        assertEquals("bob@example.com", builtDTO.email)
        assertEquals("555-9876", builtDTO.phone)
        assertEquals(false, builtDTO.active)
    }

    @Test
    fun `should verify entity relationships work`() {
        // Create a User
        val user =
            User(
                username = "memberuser",
                password = "password",
                email = "member@example.com",
                role = RoleType.MEMBER,
            )

        // Create a Member associated with the User
        val member =
            Member(
                firstName = "Church",
                lastName = "Member",
                email = "member@example.com",
                user = user,
            )

        // Verify the relationship
        val userOption = member.getUserOption()
        assertTrue(userOption.isSome())
        assertEquals(user, userOption.getOrNull())

        // Verify User authorities
        val authorities = user.authorities
        assertNotNull(authorities)
        assertTrue(authorities.any { it.authority == "ROLE_MEMBER" })
    }

    @Test
    fun `should handle optional fields correctly`() {
        // Create member with minimal required fields
        val minimalMember =
            Member(
                firstName = "Min",
                lastName = "Max",
                email = "min@example.com",
            )

        // Test all optional field accessors return None
        assertTrue(minimalMember.getPhoneNumberOption().isNone())
        assertTrue(minimalMember.getDateOfBirthOption().isNone())
        assertTrue(minimalMember.getAddressOption().isNone())
        assertTrue(minimalMember.getBaptismDateOption().isNone())
        assertTrue(minimalMember.getFamilyOption().isNone())
        assertTrue(minimalMember.getUserOption().isNone())
        assertTrue(minimalMember.getUpdatedAtOption().isNone())

        // But required fields should be accessible
        assertEquals("Min Max", minimalMember.fullName)
        assertTrue(minimalMember.isActive)
        assertNotNull(minimalMember.membershipDate)
    }
}
