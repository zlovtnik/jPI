package com.churchapp.repository

import com.churchapp.entity.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface EventRepository : JpaRepository<Event, UUID> {
    fun findByIsActive(isActive: Boolean): List<Event>

    fun findByStartDateBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): List<Event>

    fun findByLocationContaining(location: String): List<Event>
}
