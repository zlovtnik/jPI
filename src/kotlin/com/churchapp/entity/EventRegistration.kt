package com.churchapp.entity

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "event_registrations")
data class EventRegistration(
    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,
    @Column(name = "registration_date", nullable = false)
    val registrationDate: LocalDateTime = LocalDateTime.now(),
    @Column val notes: String? = null,
    @Column(name = "attended", nullable = false) val attended: Boolean = false,
) {
    // Arrow Option helpers
    fun getIdOption(): Option<UUID> = id?.some() ?: none()

    fun getNotesOption(): Option<String> = notes?.some() ?: none()
}
