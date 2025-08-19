package com.churchapp.entity

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "attendance")
data class Attendance(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,
    @Column(name = "service_date", nullable = false)
    val serviceDate: LocalDate,
    @Column(name = "service_type", nullable = false)
    val serviceType: String = "Sunday Service",
    @Column(name = "check_in_time")
    val checkInTime: LocalDateTime? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    // Arrow Option helpers
    fun getIdOption(): Option<UUID> = id?.some() ?: none()

    fun getCheckInTimeOption(): Option<LocalDateTime> = checkInTime?.some() ?: none()
}
