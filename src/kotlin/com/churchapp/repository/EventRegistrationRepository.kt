package com.churchapp.repository

import com.churchapp.entity.EventRegistration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EventRegistrationRepository : JpaRepository<EventRegistration, UUID> {
    fun findByEventId(eventId: UUID): List<EventRegistration>

    fun findByMemberId(memberId: UUID): List<EventRegistration>

    fun findByEventIdAndMemberId(
        eventId: UUID,
        memberId: UUID,
    ): EventRegistration?

    fun findByAttended(attended: Boolean): List<EventRegistration>
}
