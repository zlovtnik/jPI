package com.churchapp.service

import com.churchapp.entity.Member
import com.churchapp.repository.MemberRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`any`
import org.mockito.Mockito.`verify`
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
@DisplayName("MemberService Tests")
class MemberServiceTest {
    @Mock
    private lateinit var memberRepository: MemberRepository

    private lateinit var memberService: MemberService
    private lateinit var member: Member

    @BeforeEach
    fun setup() {
        memberService = MemberService(memberRepository)

        member =
            Member.builder()
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
    fun `should create member successfully`() {
        `when`(memberRepository.save(any(Member::class.java))).thenReturn(member)
        `when`(memberRepository.findByEmail(anyString())).thenReturn(null)

        val result = memberService.createMember(member)

        assertTrue(result.isRight())
        result.fold(
            { fail("Expected success but got error") },
            { savedMember ->
                assertNotNull(savedMember)
                assertEquals("John", savedMember.firstName)
                verify(memberRepository).save(any(Member::class.java))
            },
        )
    }

    @Test
    fun `should fail to create member when email already exists`() {
        // Create a complete existing member with all required fields
        val existingMember =
            Member.builder()
                .id(UUID.randomUUID())
                .email(member.email)
                .firstName("Existing")
                .lastName("User")
                .phoneNumber("123-456-7890")
                .address("123 Main St")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .membershipDate(LocalDate.now())
                .build()

        println("Test: Existing member email: ${existingMember.email}")
        println("Test: Member being created email: ${member.email}")

        `when`(memberRepository.findByEmail(member.email)).thenReturn(existingMember)

        val result = memberService.createMember(member)

        println("Test: Result is ${if (result.isLeft()) "Left" else "Right"}: $result")

        assertTrue(result.isLeft())
        result.fold(
            { error ->
                println("Test: Error received: $error")
                assertNotNull(error)
                assertTrue(error is MemberError.ValidationError)
                assertEquals("Validation error: Email already exists", (error as MemberError.ValidationError).message)
            },
            { success ->
                println("Test: Unexpected success: $success")
                fail("Expected error but got success")
            },
        )
    }

    @Test
    fun `should find member by id successfully`() {
        val id = UUID.randomUUID()
        member = Member.builderFrom(member).id(id).build()
        `when`(memberRepository.findById(id)).thenReturn(Optional.of(member))

        val result = memberService.findById(id)

        assertTrue(result.isRight())
        result.fold(
            { fail("Expected success but got error") },
            { foundMember ->
                assertNotNull(foundMember)
                assertEquals("John", foundMember.firstName)
            },
        )
    }

    @Test
    fun `should fail to find member by non-existent id`() {
        val id = UUID.randomUUID()
        `when`(memberRepository.findById(id)).thenReturn(Optional.empty())

        val result = memberService.findById(id)

        assertTrue(result.isLeft())
        result.fold(
            { error ->
                assertNotNull(error)
                assertTrue(error is MemberError.NotFound)
            },
            { fail("Expected error but got success") },
        )
    }

    @Test
    fun `should update member successfully`() {
        val id = UUID.randomUUID()
        val existingMember = Member.builderFrom(member).id(id).build()
        val updatedMember = Member.builderFrom(member).id(id).firstName("Jane").build()

        `when`(memberRepository.findById(id)).thenReturn(Optional.of(existingMember))
        `when`(memberRepository.findByEmail(anyString())).thenReturn(null)
        `when`(memberRepository.save(any(Member::class.java))).thenReturn(updatedMember)

        val result = memberService.updateMember(id, updatedMember)

        assertTrue(result.isRight())
        result.fold(
            { fail("Expected success but got error") },
            { savedMember ->
                assertNotNull(savedMember)
                assertEquals("Jane", savedMember.firstName)
                verify(memberRepository).save(any(Member::class.java))
            },
        )
    }

    @Test
    fun `should fail to update non-existent member`() {
        val id = UUID.randomUUID()
        `when`(memberRepository.findById(id)).thenReturn(Optional.empty())

        val result = memberService.updateMember(id, member)

        assertTrue(result.isLeft())
        result.fold(
            { error ->
                assertNotNull(error)
                assertTrue(error is MemberError.NotFound)
            },
            { fail("Expected error but got success") },
        )
    }

    @Test
    fun `should get active members successfully`() {
        val members = listOf(member)
        `when`(memberRepository.findByIsActive(true)).thenReturn(members)

        val result = memberService.getActiveMembers()

        assertTrue(result.isRight())
        result.fold(
            { fail("Expected success but got error") },
            { memberList ->
                assertNotNull(memberList)
                assertEquals(1, memberList.size)
                assertEquals("John", memberList.first().firstName)
            },
        )
    }

    @Test
    fun `should deactivate member successfully`() {
        val id = UUID.randomUUID()
        val activeMember = Member.builderFrom(member).id(id).isActive(true).build()
        val deactivatedMember = Member.builderFrom(member).id(id).isActive(false).build()

        `when`(memberRepository.findById(id)).thenReturn(Optional.of(activeMember))
        `when`(memberRepository.save(any(Member::class.java))).thenReturn(deactivatedMember)

        val result = memberService.deactivateMember(id)

        assertTrue(result.isRight())
        result.fold(
            { fail("Expected success but got error") },
            { savedMember ->
                assertNotNull(savedMember)
                assertFalse(savedMember.isActive)
                verify(memberRepository).save(any(Member::class.java))
            },
        )
    }
}
