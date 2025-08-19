package com.churchapp.repository

import com.churchapp.entity.Attendance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface AttendanceRepository : JpaRepository<Attendance, UUID> {
    fun findByMemberId(memberId: UUID): List<Attendance>

    fun findByServiceDate(serviceDate: LocalDate): List<Attendance>

    fun findByServiceType(serviceType: String): List<Attendance>

    fun findByServiceDateBetween(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Attendance>
}
