package com.churchapp.entity

import arrow.core.Option
import arrow.core.some
import arrow.core.none
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "families")
data class Family(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "family_name", nullable = false)
    @field:NotBlank(message = "Family name is required")
    val familyName: String,

    @Column
    val address: String? = null,

    @Column(name = "home_phone")
    val homePhone: String? = null,

    @OneToMany(mappedBy = "family", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val members: Set<Member> = emptySet(),

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
) {
    // Arrow Option helpers for safe nullable access
    fun getIdOption(): Option<UUID> = id?.some() ?: none()

    fun getAddressOption(): Option<String> = address?.some() ?: none()

    fun getHomePhoneOption(): Option<String> = homePhone?.some() ?: none()

    fun getUpdatedAtOption(): Option<LocalDateTime> = updatedAt?.some() ?: none()

    // Functional approach to get family head
    fun getFamilyHead(): Option<Member> =
        members.minByOrNull { it.createdAt }?.some() ?: none()
}
