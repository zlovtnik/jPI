package com.churchapp.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.util.*

@DisplayName("Member Entity Tests")
class MemberTest {

    private lateinit var member: Member
    private lateinit var family: Family

    @BeforeEach
    fun setup() {
        family = Family(
            id = UUID.randomUUID(),
            familyName = "Smith Family"
        )

        member = Member.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phoneNumber("123-456-7890")
            .address("123 Main St")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .membershipDate(LocalDate.now())
            .build()
    }

    @Test
    fun `should create member with builder pattern`() {
        assertNotNull(member)
        assertEquals("John", member.firstName)
        assertEquals("Doe", member.lastName)
        assertEquals("john.doe@example.com", member.email)
        assertTrue(member.isActive)
    }

    @Test
    fun `should get full name correctly`() {
        assertEquals("John Doe", member.fullName)
    }

    @Test
    fun `should handle phone number option`() {
        assertTrue(member.getPhoneNumberOption().isDefined())
        assertEquals("123-456-7890", member.getPhoneNumberOption().getOrNull())
    }

    @Test
    fun `should handle missing phone number`() {
        val memberWithoutPhone = Member.builder()
            .firstName("Jane")
            .lastName("Doe")
            .email("jane@example.com")
            .build()
        
        assertTrue(memberWithoutPhone.getPhoneNumberOption().isEmpty())
    }

    @Test
    fun `should handle address option`() {
        assertTrue(member.getAddressOption().isDefined())
        assertEquals("123 Main St", member.getAddressOption().getOrNull())
    }

    @Test
    fun `should handle date of birth option`() {
        assertTrue(member.getDateOfBirthOption().isDefined())
        assertEquals(LocalDate.of(1990, 1, 1), member.getDateOfBirthOption().getOrNull())
    }

    @Test
    fun `should handle family relationship`() {
        val memberWithFamily = Member.builder()
            .firstName("Child")
            .lastName("Smith")
            .email("child@example.com")
            .family(family)
            .build()
        
        assertTrue(memberWithFamily.getFamilyOption().isDefined())
        assertEquals(family, memberWithFamily.getFamilyOption().getOrNull())
    }

    @Test
    fun `should handle missing family relationship`() {
        assertTrue(member.getFamilyOption().isEmpty())
    }

    @Test
    fun `should handle baptism date option`() {
        val baptizedMember = Member.builder()
            .firstName("Baptized")
            .lastName("Member")
            .email("baptized@example.com")
            .baptismDate(LocalDate.of(2020, 1, 1))
            .build()
        
        assertTrue(baptizedMember.getBaptismDateOption().isDefined())
        assertEquals(LocalDate.of(2020, 1, 1), baptizedMember.getBaptismDateOption().getOrNull())
    }

    @Test
    fun `should have default active status`() {
        assertTrue(member.isActive)
    }

    @Test
    fun `should handle inactive member`() {
        val inactiveMember = Member.builder()
            .firstName("Inactive")
            .lastName("Member")
            .email("inactive@example.com")
            .isActive(false)
            .build()
        
        assertFalse(inactiveMember.isActive)
    }

    @Test
    fun `should build member with all properties`() {
        val builtMember = Member.builder()
            .firstName("Jane")
            .lastName("Smith")
            .email("jane.smith@example.com")
            .phoneNumber("098-765-4321")
            .address("456 Oak St")
            .dateOfBirth(LocalDate.of(1992, 6, 15))
            .membershipDate(LocalDate.now())
            .baptismDate(LocalDate.of(2010, 1, 1))
            .family(family)
            .build()

        assertEquals("Jane", builtMember.firstName)
        assertEquals("Smith", builtMember.lastName)
        assertEquals("jane.smith@example.com", builtMember.email)
        assertEquals("098-765-4321", builtMember.getPhoneNumberOption().getOrNull())
        assertEquals("456 Oak St", builtMember.getAddressOption().getOrNull())
        assertEquals(family, builtMember.getFamilyOption().getOrNull())
    }
}
