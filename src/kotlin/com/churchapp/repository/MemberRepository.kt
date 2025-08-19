package com.churchapp.repository

import com.churchapp.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MemberRepository : JpaRepository<Member, UUID> {
    fun findByEmail(email: String): Member?
    fun findByIsActive(isActive: Boolean): List<Member>
    fun existsByEmail(email: String): Boolean
    fun findByFamilyId(familyId: UUID): List<Member>
}
