package com.churchapp.service

import io.vavr.control.Try
import com.churchapp.entity.Donation
import com.churchapp.entity.Member
import com.churchapp.entity.enums.DonationType
import com.churchapp.repository.DonationRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class DonationService(
    private val donationRepository: DonationRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(DonationService::class.java)

    // Create donation with functional validation 
    fun createDonation(donation: Donation): Try<Donation> {
        return Try.of {
            // Basic validation
            require(donation.amount.compareTo(BigDecimal.ZERO) > 0) { "Donation amount must be positive" }
            require(donation.donationType != null) { "Donation type is required" }
            
            val savedDonation = donationRepository.save(donation)
            eventPublisher.publishEvent(DonationCreatedEvent(savedDonation))
            savedDonation
        }
    }

    // Update donation
    fun updateDonation(donation: Donation): Try<Donation> {
        return Try.of {
            require(donation.id != null) { "Donation ID is required for update" }
            require(donation.amount.compareTo(BigDecimal.ZERO) > 0) { "Donation amount must be positive" }
            
            donationRepository.save(donation)
        }
    }

    // Get all donations
    fun getAllDonations(): List<Donation> {
        return donationRepository.findAll()
    }

    // Get donation by ID
    fun getDonationById(id: UUID): Optional<Donation> {
        return donationRepository.findById(id)
    }

    // Get donations by member
    fun getDonationsByMember(memberId: UUID): List<Donation> {
        return donationRepository.findByMemberId(memberId)
    }

    // Get total donations by member
    fun getTotalDonationsByMember(memberId: UUID): BigDecimal {
        return donationRepository.findByMemberId(memberId)
            .fold(BigDecimal.ZERO) { acc, donation -> acc + donation.amount }
    }

    // Get donations by date range
    fun getDonationsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Donation> {
        return donationRepository.findByDonationDateBetween(startDate, endDate)
    }

    // Delete donation
    fun deleteDonation(id: UUID): Try<Void> {
        return Try.of {
            donationRepository.deleteById(id)
            null
        }
    }

    // Event for donation creation
    data class DonationCreatedEvent(val donation: Donation)
}
