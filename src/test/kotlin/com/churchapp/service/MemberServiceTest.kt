package com.churchapp.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.churchapp.entity.Member
import com.churchapp.repository.MemberRepository
import com.churchapp.service.MemberService
import com.churchapp.service.MemberError
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.util.*

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
    fun `should create member successfully`() {
        `when`(memberRepository.save(any(Member::class.java))).thenReturn(member)
        `when`(memberRepository.existsByEmail(anyString())).thenReturn(false)

        val result = memberService.createMember(member)

        assertTrue(result.isRight())
        result.fold(
            { fail("Expected success but got error") },
            { savedMember ->
                assertNotNull(savedMember)
                assertEquals("John", savedMember.firstName)
                verify(memberRepository).save(any(Member::class.java))
            }
        )
    }

    @Test
    fun `should fail to create member when email already exists`() {
        `when`(memberRepository.existsByEmail(anyString())).thenReturn(true)

        val result = memberService.createMember(member)

        assertTrue(result.isLeft())
        result.fold(
            { error ->
                assertNotNull(error)
                assertTrue(error is MemberError.ValidationError)
            },
            { fail("Expected error but got success") }
        )
    }

    @Test
    fun `should find member by id successfully`() {
        val id = UUID.randomUUID()
        member = member.copy(id = id)
        `when`(memberRepository.findById(id)).thenReturn(Optional.of(member))

        val result = memberService.findById(id)

        assertTrue(result.isRight())
        result.fold(
            { fail("Expected success but got error") },
            { foundMember ->
                assertNotNull(foundMember)
                assertEquals("John", foundMember.firstName)
            }
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
            { fail("Expected error but got success") }
        )
    }

    @Test
    fun `should update member successfully`() {
        val id = UUID.randomUUID()
        val existingMember = member.copy(id = id)
        val updatedMember = member.copy(id = id, firstName = "Jane")
        
        `when`(memberRepository.findById(id)).thenReturn(Optional.of(existingMember))
        `when`(memberRepository.save(any(Member::class.java))).thenReturn(updatedMember)
        `when`(memberRepository.existsByEmail(anyString())).thenReturn(false)

        val result = memberService.updateMember(id, updatedMember)

        assertTrue(result.isRight())
        result.fold(
            { fail("Expected success but got error") },
            { savedMember ->
                assertNotNull(savedMember)
                assertEquals("Jane", savedMember.firstName)
                verify(memberRepository).save(any(Member::class.java))
            }
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
            { fail("Expected error but got success") }
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
            }
        )
    }

    @Test
    fun `should deactivate member successfully`() {
        val id = UUID.randomUUID()
        val activeMember = member.copy(id = id, isActive = true)
        val deactivatedMember = member.copy(id = id, isActive = false)
        
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
            }
        )
    }
}
