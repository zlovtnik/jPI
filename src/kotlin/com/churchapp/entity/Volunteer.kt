package com.churchapp.entity

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "volunteers")
data class Volunteer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,
    @Column(nullable = false)
    @field:NotBlank(message = "Role is required")
    val role: String,
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    @Column(name = "start_date", nullable = false)
    val startDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "end_date")
    val endDate: LocalDateTime? = null,
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    // Arrow Option helpers
    fun getIdOption(): Option<UUID> = Option.fromNullable(id)

    fun getDescriptionOption(): Option<String> = Option.fromNullable(description)

    fun getEndDateOption(): Option<LocalDateTime> = Option.fromNullable(endDate)

    // Check if volunteer assignment is current
    fun isCurrent(): Boolean =
        isActive &&
            when (val endDateOption = getEndDateOption()) {
                is None -> true
                is Some -> endDateOption.value.isAfter(LocalDateTime.now())
            }
}
