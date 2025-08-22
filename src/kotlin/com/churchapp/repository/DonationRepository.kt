package com.churchapp.repository

import com.churchapp.entity.Donation
import com.churchapp.entity.enums.DonationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface DonationRepository : JpaRepository<Donation, UUID> {
    fun findByMemberId(memberId: UUID): List<Donation>

    fun findByDonationType(donationType: DonationType): List<Donation>

    fun findByDonationTypeAndDonationDateBetween(
        donationType: DonationType,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): List<Donation>

    fun findByDonationDateBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): List<Donation>

    fun findByIsAnonymous(isAnonymous: Boolean): List<Donation>
}
