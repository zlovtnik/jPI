package com.churchapp.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.churchapp.entity.Donation
import com.churchapp.repository.DonationRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Service
class DonationService(
    private val donationRepository: DonationRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = LoggerFactory.getLogger(DonationService::class.java)

    // Create donation with functional validation
    @org.springframework.transaction.annotation.Transactional
    fun createDonation(donation: Donation): Either<Exception, Donation> =
        try {
            require(donation.amount.compareTo(BigDecimal.ZERO) > 0) { "Donation amount must be positive" }

            val savedDonation = donationRepository.save(donation)
            try {
                eventPublisher.publishEvent(DonationCreatedEvent(savedDonation))
            } catch (eventEx: Exception) {
                logger.error("Failed to publish DonationCreatedEvent", eventEx)
            }
            savedDonation.right()
        } catch (e: Exception) {
            logger.error("Error creating donation", e)
            e.left()
        }

    // Update donation
    fun updateDonation(donation: Donation): Either<Exception, Donation> =
        try {
            require(donation.id != null) { "Donation ID is required for update" }
            require(donation.amount.compareTo(BigDecimal.ZERO) > 0) { "Donation amount must be positive" }

            donationRepository.save(donation).right()
        } catch (e: Exception) {
            logger.error("Error updating donation", e)
            e.left()
        }

    // Get all donations
    fun getAllDonations(): List<Donation> =
        try {
            donationRepository.findAll()
        } catch (e: Exception) {
            logger.error("Error retrieving all donations", e)
            emptyList()
        }

    // Get donation by ID
    fun getDonationById(id: UUID): Optional<Donation> {
        return donationRepository.findById(id)
    }

    // Get donations by member
    fun getDonationsByMember(memberId: UUID): List<Donation> {
        return donationRepository.findByMemberId(memberId)
    }

    fun getTotalDonationsByMember(memberId: UUID): BigDecimal {
        return donationRepository.findByMemberId(memberId)
            .fold(BigDecimal.ZERO) { acc, donation ->
                acc.add(requireNotNull(donation.amount) { "Donation amount is required" })
            }
    }

    // Get donations by date range
    fun getDonationsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): List<Donation> {
        return donationRepository.findByDonationDateBetween(startDate, endDate)
    }

    // Delete donation
    fun deleteDonation(id: UUID): Either<Exception, Unit> =
        try {
            donationRepository.deleteById(id)
            Unit.right()
        } catch (e: Exception) {
            logger.error("Error deleting donation", e)
            e.left()
        }

    // Event for donation creation
    data class DonationCreatedEvent(val donation: Donation)
}
