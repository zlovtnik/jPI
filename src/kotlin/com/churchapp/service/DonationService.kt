package com.churchapp.service

import arrow.core.Either
import arrow.core.Option
import arrow.core.None
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import com.churchapp.entity.Donation
import com.churchapp.entity.Member
import com.churchapp.entity.enums.DonationType
import com.churchapp.repository.DonationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class DonationService(
    private val donationRepository: DonationRepository,
    private val memberService: MemberService
) {

    private val logger = LoggerFactory.getLogger(DonationService::class.java)

    // Create donation with functional validation and member lookup
    fun createDonation(donation: Donation): Either<DonationError, Donation> {
        val validationResult = validateDonation(donation)
        return when (validationResult) {
            is Either.Left -> validationResult
            is Either.Right -> {
                val validDonation = validationResult.value
                val memberOption = validDonation.getMemberOption()

                when (memberOption) {
                    is None -> {
                        // Anonymous donation, proceed to save
                        try {
                            donationRepository.save(validDonation).right()
                        } catch (e: Exception) {
                            logger.error("Error creating donation", e)
                            DonationError.DatabaseError(e.message ?: "Failed to create donation").left()
                        }
                    }
                    is Some -> {
                        // Verify member exists
                        val member = memberOption.value
                        val memberResult = memberService.findById(member.id!!)
                        when (memberResult) {
                            is Either.Left -> DonationError.MemberNotFound(member.id!!).left()
                            is Either.Right -> {
                                try {
                                    donationRepository.save(validDonation).right()
                                } catch (e: Exception) {
                                    logger.error("Error creating donation", e)
                                    DonationError.DatabaseError(e.message ?: "Failed to create donation").left()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Get donations by member with functional approach
    fun getDonationsByMember(memberId: UUID): Either<DonationError, List<Donation>> = try {
        donationRepository.findByMemberId(memberId).right()
    } catch (e: Exception) {
        logger.error("Error retrieving donations for member: $memberId", e)
        DonationError.DatabaseError(e.message ?: "Failed to retrieve donations").left()
    }

    // Get donations by type and date range
    fun getDonationsByTypeAndDateRange(
        donationType: DonationType,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Either<DonationError, List<Donation>> = try {
        donationRepository.findByDonationTypeAndDonationDateBetween(donationType, startDate, endDate).right()
    } catch (e: Exception) {
        logger.error("Error retrieving donations by type and date range", e)
        DonationError.DatabaseError(e.message ?: "Failed to retrieve donations").left()
    }

    // Calculate total donations
    fun calculateTotalDonations(donations: List<Donation>): BigDecimal =
        donations.fold(BigDecimal.ZERO) { acc, donation -> acc + donation.amount }

    // Get donation statistics
    fun getDonationStatistics(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Either<DonationError, DonationStatistics> {
        return try {
            val results = mutableMapOf<DonationType, BigDecimal>()

            for (donationType in DonationType.values()) {
                val donationsResult = getDonationsByTypeAndDateRange(donationType, startDate, endDate)
                when (donationsResult) {
                    is Either.Left -> return donationsResult
                    is Either.Right -> {
                        val total = calculateTotalDonations(donationsResult.value)
                        results[donationType] = total
                    }
                }
            }

            val totalAmount = results.values.fold(BigDecimal.ZERO) { acc, amount -> acc + amount }

            DonationStatistics(
                totalAmount = totalAmount,
                donationsByType = results,
                period = DateRange(startDate, endDate)
            ).right()
        } catch (e: Exception) {
            logger.error("Error calculating donation statistics", e)
            DonationError.DatabaseError(e.message ?: "Failed to calculate statistics").left()
        }
    }

    // Functional validation
    private fun validateDonation(donation: Donation): Either<DonationError, Donation> = when {
        donation.amount <= BigDecimal.ZERO ->
            DonationError.ValidationError("Donation amount must be greater than 0").left()
        donation.donationDate.isAfter(LocalDateTime.now()) ->
            DonationError.ValidationError("Donation date cannot be in the future").left()
        else -> donation.right()
    }

    // Find donation by ID with Option
    fun findByIdOption(id: UUID): Option<Donation> = try {
        val optional = donationRepository.findById(id)
        Option.fromNullable(optional.orElse(null))
    } catch (e: Exception) {
        logger.error("Error finding donation by id: $id", e)
        None
    }
}

// Sealed class for donation errors
sealed class DonationError(val message: String) {
    data class ValidationError(val details: String) : DonationError("Validation error: $details")
    data class MemberNotFound(val memberId: UUID) : DonationError("Member with id $memberId not found")
    data class DatabaseError(val details: String) : DonationError("Database error: $details")
}

// Data classes for statistics
data class DonationStatistics(
    val totalAmount: BigDecimal,
    val donationsByType: Map<DonationType, BigDecimal>,
    val period: DateRange
)

data class DateRange(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)
