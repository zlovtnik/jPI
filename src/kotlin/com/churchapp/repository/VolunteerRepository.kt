package com.churchapp.repository

import com.churchapp.entity.Volunteer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VolunteerRepository : JpaRepository<Volunteer, UUID> {
    fun findByMemberId(memberId: UUID): List<Volunteer>

    fun findByIsActive(isActive: Boolean): List<Volunteer>

    fun findByRole(role: String): List<Volunteer>
}
