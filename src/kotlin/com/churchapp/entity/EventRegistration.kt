package com.churchapp.entity

import arrow.core.Option
import arrow.core.some
import arrow.core.none
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "event_registrations")
data class EventRegistration(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "registration_date", nullable = false)
    val registrationDate: LocalDateTime = LocalDateTime.now(),

    @Column
    val notes: String? = null,

    @Column(name = "attended", nullable = false)
    val attended: Boolean = false
) {
    // Arrow Option helpers
    fun getIdOption(): Option<UUID> = id?.some() ?: none()

    fun getNotesOption(): Option<String> = notes?.some() ?: none()
}
