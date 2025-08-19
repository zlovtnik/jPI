package com.churchapp.entity

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "members")
class Member(
    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: UUID? = null,
    @Column(name = "first_name", nullable = false)
    @field:NotBlank(message = "First name is required")
    val firstName: String,
    @Column(name = "last_name", nullable = false)
    @field:NotBlank(message = "Last name is required")
    val lastName: String,
    @Column(unique = true, nullable = false)
    @field:Email(message = "Valid email is required")
    val email: String,
    @Column(name = "phone_number") val phoneNumber: String? = null,
    @Column(name = "date_of_birth") val dateOfBirth: LocalDate? = null,
    @Column val address: String? = null,
    @Column(name = "membership_date", nullable = false)
    val membershipDate: LocalDate = LocalDate.now(),
    @Column(name = "baptism_date") val baptismDate: LocalDate? = null,
    @Column(name = "is_active", nullable = false) val isActive: Boolean = true,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    val family: Family? = null,
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at") val updatedAt: LocalDateTime? = null,
) {
    val fullName: String
        get() = "$firstName $lastName"

    fun getIdOption(): Option<UUID> = id?.some() ?: none()

    fun getPhoneNumberOption(): Option<String> = phoneNumber?.some() ?: none()

    fun getDateOfBirthOption(): Option<LocalDate> = dateOfBirth?.some() ?: none()

    fun getAddressOption(): Option<String> = address?.some() ?: none()

    fun getBaptismDateOption(): Option<LocalDate> = baptismDate?.some() ?: none()

    fun getFamilyOption(): Option<Family> = family?.some() ?: none()

    fun getUserOption(): Option<User> = user?.some() ?: none()

    fun getUpdatedAtOption(): Option<LocalDateTime> = updatedAt?.some() ?: none()

    companion object {
        @JvmStatic fun builder(): Builder = Builder()

        @JvmStatic fun builderFrom(existing: Member): Builder = Builder().copyFrom(existing)
    }

    fun toBuilder(): Builder = Builder().copyFrom(this)

    // Extension function for Builder

    // Extension function to copy fields from Member to Builder, scoped inside Member for visibility
    fun Builder.copyFrom(member: Member): Builder =
        this.id(member.id)
            .firstName(member.firstName)
            .lastName(member.lastName)
            .email(member.email)
            .phoneNumber(member.phoneNumber)
            .dateOfBirth(member.dateOfBirth)
            .address(member.address)
            .membershipDate(member.membershipDate)
            .baptismDate(member.baptismDate)
            .isActive(member.isActive)
            .family(member.family)
            .user(member.user)
            .createdAt(member.createdAt)
            .updatedAt(member.updatedAt)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false
        if (id == null || other.id == null) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: email.hashCode()

    override fun toString(): String = "Member(id=$id, fullName=$fullName, email=$email)"

    class Builder internal constructor() {
        private var id: UUID? = null
        private var firstName: String = ""
        private var lastName: String = ""
        private var email: String = ""
        private var phoneNumber: String? = null
        private var dateOfBirth: LocalDate? = null
        private var address: String? = null
        private var membershipDate: LocalDate = LocalDate.now()
        private var baptismDate: LocalDate? = null
        private var isActive: Boolean = true
        private var family: Family? = null
        private var user: User? = null
        private var createdAt: LocalDateTime = LocalDateTime.now()
        private var updatedAt: LocalDateTime? = null

        fun id(id: UUID?): Builder = apply { this.id = id }

        fun firstName(firstName: String): Builder = apply { this.firstName = firstName }

        fun lastName(lastName: String): Builder = apply { this.lastName = lastName }

        fun email(email: String): Builder = apply { this.email = email }

        fun phoneNumber(phoneNumber: String?): Builder = apply { this.phoneNumber = phoneNumber }

        fun dateOfBirth(dateOfBirth: LocalDate?): Builder = apply { this.dateOfBirth = dateOfBirth }

        fun address(address: String?): Builder = apply { this.address = address }

        fun membershipDate(membershipDate: LocalDate): Builder =
            apply {
                this.membershipDate = membershipDate
            }

        fun baptismDate(baptismDate: LocalDate?): Builder = apply { this.baptismDate = baptismDate }

        fun isActive(isActive: Boolean): Builder = apply { this.isActive = isActive }

        fun family(family: Family?): Builder = apply { this.family = family }

        fun user(user: User?): Builder = apply { this.user = user }

        fun createdAt(createdAt: LocalDateTime): Builder = apply { this.createdAt = createdAt }

        fun updatedAt(updatedAt: LocalDateTime?): Builder = apply { this.updatedAt = updatedAt }

        fun copyFrom(member: Member): Builder =
            this.id(member.id)
                .firstName(member.firstName)
                .lastName(member.lastName)
                .email(member.email)
                .phoneNumber(member.phoneNumber)
                .dateOfBirth(member.dateOfBirth)
                .address(member.address)
                .membershipDate(member.membershipDate)
                .baptismDate(member.baptismDate)
                .isActive(member.isActive)
                .family(member.family)
                .user(member.user)
                .createdAt(member.createdAt)
                .updatedAt(member.updatedAt)

        fun build(): Member {
            val normFirstName = firstName.trim()
            val normLastName = lastName.trim()

            val normEmail = email.trim().lowercase()

            if (normFirstName.isBlank()) throw IllegalStateException("First name cannot be blank")
            if (normLastName.isBlank()) throw IllegalStateException("Last name cannot be blank")
            if (normEmail.isBlank()) throw IllegalStateException("Valid email is required")

            val normPhone = phoneNumber?.trim()?.takeIf { it.isNotBlank() }
            val normAddress = address?.trim()?.takeIf { it.isNotBlank() }

            return Member(
                id = id,
                firstName = normFirstName,
                lastName = normLastName,
                email = normEmail,
                phoneNumber = normPhone,
                dateOfBirth = dateOfBirth,
                address = normAddress,
                membershipDate = membershipDate,
                baptismDate = baptismDate,
                isActive = isActive,
                family = family,
                user = user,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }
}
