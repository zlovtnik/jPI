package com.churchapp.entity

import arrow.core.Option
import arrow.core.some
import arrow.core.none
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "members")
data class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "first_name", nullable = false)
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @Column(name = "last_name", nullable = false)
    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @Column(unique = true, nullable = false)
    @field:Email(message = "Valid email is required")
    val email: String,

    @Column(name = "phone_number")
    val phoneNumber: String? = null,

    @Column(name = "date_of_birth")
    val dateOfBirth: LocalDate? = null,

    @Column
    val address: String? = null,

    @Column(name = "membership_date", nullable = false)
    val membershipDate: LocalDate = LocalDate.now(),

    @Column(name = "baptism_date")
    val baptismDate: LocalDate? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    val family: Family? = null,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
) {
    // Computed property using Arrow Option
    val fullName: String
        get() = "$firstName $lastName"

    // Arrow Option helpers for safe nullable access
    fun getIdOption(): Option<UUID> = id?.some() ?: none()

    fun getPhoneNumberOption(): Option<String> = phoneNumber?.some() ?: none()

    fun getDateOfBirthOption(): Option<LocalDate> = dateOfBirth?.some() ?: none()

    fun getAddressOption(): Option<String> = address?.some() ?: none()

    fun getBaptismDateOption(): Option<LocalDate> = baptismDate?.some() ?: none()

    fun getFamilyOption(): Option<Family> = family?.some() ?: none()

    fun getUserOption(): Option<User> = user?.some() ?: none()

    fun getUpdatedAtOption(): Option<LocalDateTime> = updatedAt?.some() ?: none()

    companion object {
        @JvmStatic
        fun builder(): MemberBuilder {
            return MemberBuilder()
        }
    }
}

class MemberBuilder {
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

    fun id(id: UUID?) = apply { this.id = id }
    fun firstName(firstName: String) = apply { this.firstName = firstName }
    fun lastName(lastName: String) = apply { this.lastName = lastName }
    fun email(email: String) = apply { this.email = email }
    fun phoneNumber(phoneNumber: String?) = apply { this.phoneNumber = phoneNumber }
    fun dateOfBirth(dateOfBirth: LocalDate?) = apply { this.dateOfBirth = dateOfBirth }
    fun address(address: String?) = apply { this.address = address }
    fun membershipDate(membershipDate: LocalDate) = apply { this.membershipDate = membershipDate }
    fun baptismDate(baptismDate: LocalDate?) = apply { this.baptismDate = baptismDate }
    fun isActive(isActive: Boolean) = apply { this.isActive = isActive }
    fun family(family: Family?) = apply { this.family = family }
    fun user(user: User?) = apply { this.user = user }
    fun createdAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
    fun updatedAt(updatedAt: LocalDateTime?) = apply { this.updatedAt = updatedAt }

    fun build(): Member {
        return Member(id, firstName, lastName, email, phoneNumber, dateOfBirth, address, 
                     membershipDate, baptismDate, isActive, family, user, createdAt, updatedAt)
    }
}
