package com.churchapp.entity

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.churchapp.entity.enums.DonationType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "donations")
data class Donation(
    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: UUID? = null,
    @Column(nullable = false, precision = 10, scale = 2)
    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    val amount: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(name = "donation_type", nullable = false)
    val donationType: DonationType,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member? = null,
    @Column(name = "anonymous", nullable = false) val isAnonymous: Boolean = false,
    @Column val notes: String? = null,
    @Column(name = "donation_date", nullable = false)
    val donationDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    // Arrow Option helpers for safe nullable access
    fun getIdOption(): Option<UUID> = Option.fromNullable(id)

    fun getMemberOption(): Option<Member> = Option.fromNullable(member)

    fun getNotesOption(): Option<String> = Option.fromNullable(notes)

    fun getIsAnonymous(): Boolean = isAnonymous

    // Functional helper to get donor name or anonymous
    fun getDonorName(): String =
        if (isAnonymous) {
            "Anonymous"
        } else {
            when (val memberOption = getMemberOption()) {
                is None -> "Unknown"
                is Some -> "${memberOption.value.firstName} ${memberOption.value.lastName}"
            }
        }
}
