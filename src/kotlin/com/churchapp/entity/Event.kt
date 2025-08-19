package com.churchapp.entity

import arrow.core.Option
import arrow.core.None
import arrow.core.Some
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "events")
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    @field:NotBlank(message = "Event name is required")
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "start_date", nullable = false)
    @field:NotNull(message = "Start date is required")
    val startDate: LocalDateTime,

    @Column(name = "end_date")
    val endDate: LocalDateTime? = null,

    @Column
    val location: String? = null,

    @Column(name = "max_capacity")
    val maxCapacity: Int? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val registrations: Set<EventRegistration> = emptySet(),

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
) {
    // Arrow Option helpers for safe nullable access
    fun getIdOption(): Option<UUID> = Option.fromNullable(id)

    fun getDescriptionOption(): Option<String> = Option.fromNullable(description)

    fun getEndDateOption(): Option<LocalDateTime> = Option.fromNullable(endDate)

    fun getLocationOption(): Option<String> = Option.fromNullable(location)

    fun getMaxCapacityOption(): Option<Int> = Option.fromNullable(maxCapacity)

    fun getIsActive(): Boolean = isActive

    fun getUpdatedAtOption(): Option<LocalDateTime> = Option.fromNullable(updatedAt)

    // Functional helper to check if event is full
    fun isFull(): Boolean = when (val maxCapacityOption = getMaxCapacityOption()) {
        is None -> false
        is Some -> registrations.size >= maxCapacityOption.value
    }

    // Get available spots using functional approach
    fun getAvailableSpots(): Option<Int> = when (val maxCapacityOption = getMaxCapacityOption()) {
        is None -> None
        is Some -> Some((maxCapacityOption.value - registrations.size).coerceAtLeast(0))
    }
}
