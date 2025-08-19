package com.churchapp.entity

import arrow.core.Option
import arrow.core.None
import arrow.core.Some
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "groups")
data class Group(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    @field:NotBlank(message = "Group name is required")
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    val leader: Member? = null,

    @Column(name = "max_members")
    val maxMembers: Int? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_members",
        joinColumns = [JoinColumn(name = "group_id")],
        inverseJoinColumns = [JoinColumn(name = "member_id")]
    )
    val members: Set<Member> = emptySet(),

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
) {
    // Arrow Option helpers
    fun getIdOption(): Option<UUID> = Option.fromNullable(id)

    fun getDescriptionOption(): Option<String> = Option.fromNullable(description)

    fun getLeaderOption(): Option<Member> = Option.fromNullable(leader)

    fun getMaxMembersOption(): Option<Int> = Option.fromNullable(maxMembers)

    fun getUpdatedAtOption(): Option<LocalDateTime> = Option.fromNullable(updatedAt)

    fun getIsActive(): Boolean = isActive

    // Check if group is full
    fun isFull(): Boolean = when (val maxMembersOption = getMaxMembersOption()) {
        is None -> false
        is Some -> members.size >= maxMembersOption.value
    }

    // Get available spots
    fun getAvailableSpots(): Option<Int> = when (val maxMembersOption = getMaxMembersOption()) {
        is None -> None
        is Some -> Some((maxMembersOption.value - members.size).coerceAtLeast(0))
    }
}
