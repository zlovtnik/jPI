package com.churchapp.repository

import com.churchapp.entity.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GroupRepository : JpaRepository<Group, UUID> {
    fun findByIsActive(isActive: Boolean): List<Group>

    fun findByLeaderId(leaderId: UUID): List<Group>

    fun findByNameContaining(name: String): List<Group>
}
