package com.churchapp.service

import arrow.core.Either
import com.churchapp.entity.Donation
import com.churchapp.entity.Member
import com.churchapp.entity.enums.DonationType
import com.churchapp.repository.DonationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DonationServiceTest {
    @Mock private lateinit var donationRepository: DonationRepository

    @Mock private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks private lateinit var donationService: DonationService

    private lateinit var testDonation: Donation
    private lateinit var testMember: Member

    @BeforeEach
    fun setUp() {
        testMember =
            Member(
                UUID.randomUUID(),
                "John",
                "Doe",
                "john.doe@example.com",
                null,
                null,
                null,
                LocalDate.now(),
                null,
                true,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
            )

        testDonation =
            Donation(
                UUID.randomUUID(),
                BigDecimal("100.00"),
                DonationType.TITHE,
                testMember,
                false,
                "Test Description",
                LocalDateTime.now(),
                LocalDateTime.now(),
            )
    }

    @Test
    fun `createDonation should return successful result when valid donation`() {
        // Given
        Mockito.`when`(donationRepository.save(any(Donation::class.java))).thenReturn(testDonation)

        // When
        val result: Either<Exception, Donation> = donationService.createDonation(testDonation)

        // Then
        assertTrue(result.isRight())
        assertEquals(testDonation, result.getOrNull())
        verify(donationRepository).save(testDonation)
        verify(eventPublisher).publishEvent(any(DonationService.DonationCreatedEvent::class.java))
    }

    @Test
    fun `createDonation should return failure when repository throws exception`() {
        // Given
        Mockito.`when`(donationRepository.save(any(Donation::class.java)))
            .thenThrow(RuntimeException("Database error"))

        // When
        val result = donationService.createDonation(testDonation)

        // Then
        assertTrue(result.isLeft())
        assertNotNull(result.leftOrNull())
        assertTrue(result.leftOrNull() is RuntimeException)
    }

    @Test
    fun `updateDonation should return successful result when valid donation`() {
        // Given
        val updatedDonation =
            Donation(
                testDonation.id,
                BigDecimal("150.00"),
                testDonation.donationType,
                testDonation.member,
                testDonation.isAnonymous,
                testDonation.notes,
                testDonation.donationDate,
                testDonation.createdAt,
            )
        Mockito.`when`(donationRepository.save(any(Donation::class.java)))
            .thenReturn(updatedDonation)

        // When
        val result = donationService.updateDonation(updatedDonation)

        // Then
        assertTrue(result.isRight())
        assertEquals(updatedDonation, result.getOrNull())
        verify(donationRepository).save(updatedDonation)
    }

    @Test
    fun `updateDonation should return failure when repository throws exception`() {
        // Given
        Mockito.`when`(donationRepository.save(any(Donation::class.java)))
            .thenThrow(RuntimeException("Update failed"))

        // When
        val result = donationService.updateDonation(testDonation)

        // Then
        assertTrue(result.isLeft())
        assertNotNull(result.leftOrNull())
        assertTrue(result.leftOrNull() is RuntimeException)
    }

    @Test
    fun `getAllDonations should return list of donations when donations exist`() {
        // Given
        val donations = listOf(testDonation)
        Mockito.`when`(donationRepository.findAll()).thenReturn(donations)

        // When
        val result = donationService.getAllDonations()

        // Then
        assertFalse(result.isEmpty())
        assertEquals(1, result.size)
        assertTrue(result.contains(testDonation))
    }

    @Test
    fun `getAllDonations should return empty list when no donations exist`() {
        // Given
        Mockito.`when`(donationRepository.findAll()).thenReturn(emptyList())

        // When
        val result = donationService.getAllDonations()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllDonations should return empty list when repository throws exception`() {
        // Given
        Mockito.`when`(donationRepository.findAll()).thenThrow(RuntimeException("Database error"))

        // When
        val result = donationService.getAllDonations()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getDonationById should return some when donation exists`() {
        // Given
        val donationId = UUID.randomUUID()
        Mockito.`when`(donationRepository.findById(donationId))
            .thenReturn(Optional.of(testDonation))

        // When
        val result = donationService.getDonationById(donationId)

        // Then
        assertTrue(result.isPresent)
        assertEquals(testDonation, result.get())
    }

    @Test
    fun `getDonationById should return none when donation does not exist`() {
        // Given
        val donationId = UUID.randomUUID()
        Mockito.`when`(donationRepository.findById(donationId)).thenReturn(Optional.empty())

        // When
        val result = donationService.getDonationById(donationId)

        // Then
        assertFalse(result.isPresent)
    }

    @Test
    fun `getDonationsByMember should return donations when member has donations`() {
        // Given
        val memberId = UUID.randomUUID()
        val donations = listOf(testDonation)
        Mockito.`when`(donationRepository.findByMemberId(memberId)).thenReturn(donations)

        // When
        val result = donationService.getDonationsByMember(memberId)

        // Then
        assertFalse(result.isEmpty())
        assertEquals(1, result.size)
    }

    @Test
    fun `getTotalDonationsByMember should return correct sum`() {
        // Given
        val memberId = UUID.randomUUID()
        val donations = listOf(testDonation)
        Mockito.`when`(donationRepository.findByMemberId(memberId)).thenReturn(donations)

        // When
        val result = donationService.getTotalDonationsByMember(memberId)

        // Then
        assertEquals(BigDecimal("100.00"), result)
    }

    @Test
    fun `getDonationsByDateRange should return donations in range`() {
        // Given
        val startDate = LocalDateTime.now().minusMonths(1)
        val endDate = LocalDateTime.now()
        val donations = listOf(testDonation)
        Mockito.`when`(donationRepository.findByDonationDateBetween(startDate, endDate))
            .thenReturn(donations)

        // When
        val result = donationService.getDonationsByDateRange(startDate, endDate)

        // Then
        assertFalse(result.isEmpty())
        assertEquals(1, result.size)
        assertTrue(result.contains(testDonation))
    }

    @Test
    fun `deleteDonation should return successful result when donation exists`() {
        // Given
        val donationId = UUID.randomUUID()
        doNothing().`when`(donationRepository).deleteById(donationId)

        // When
        val result = donationService.deleteDonation(donationId)

        // Then
        assertTrue(result.isRight())
        verify(donationRepository).deleteById(donationId)
    }

    @Test
    fun `deleteDonation should return failure when repository throws exception`() {
        // Given
        val donationId = UUID.randomUUID()
        doThrow(RuntimeException("Delete failed")).`when`(donationRepository).deleteById(donationId)

        // When
        val result = donationService.deleteDonation(donationId)

        // Then
        assertTrue(result.isLeft())
        assertNotNull(result.leftOrNull())
        assertTrue(result.leftOrNull() is RuntimeException)
    }
}
