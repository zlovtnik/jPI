package com.churchapp.repository

import com.churchapp.entity.Family
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FamilyRepository : JpaRepository<Family, UUID> {
    fun findByFamilyNameContaining(familyName: String): List<Family>

    fun findByAddressContaining(address: String): List<Family>
}
