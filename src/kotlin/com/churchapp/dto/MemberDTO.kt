package com.churchapp.dto

import java.time.LocalDate
import java.util.UUID

data class MemberDTO(
    var id: UUID? = null,
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phone: String? = null,
    var dateOfBirth: LocalDate? = null,
    var membershipDate: LocalDate? = null,
    var address: String? = null,
    var familyId: Int? = null,
    var active: Boolean = true
) {
    companion object {
        @JvmStatic
        fun builder(): MemberDTOBuilder {
            return MemberDTOBuilder()
        }
    }
}

class MemberDTOBuilder {
    private var id: UUID? = null
    private var firstName: String = ""
    private var lastName: String = ""
    private var email: String = ""
    private var phone: String? = null
    private var dateOfBirth: LocalDate? = null
    private var membershipDate: LocalDate? = null
    private var address: String? = null
    private var familyId: Int? = null
    private var active: Boolean = true

    fun id(id: UUID?) = apply { this.id = id }
    fun firstName(firstName: String) = apply { this.firstName = firstName }
    fun lastName(lastName: String) = apply { this.lastName = lastName }
    fun email(email: String) = apply { this.email = email }
    fun phone(phone: String?) = apply { this.phone = phone }
    fun dateOfBirth(dateOfBirth: LocalDate?) = apply { this.dateOfBirth = dateOfBirth }
    fun membershipDate(membershipDate: LocalDate?) = apply { this.membershipDate = membershipDate }
    fun address(address: String?) = apply { this.address = address }
    fun familyId(familyId: Int?) = apply { this.familyId = familyId }
    fun active(active: Boolean) = apply { this.active = active }

    fun build(): MemberDTO {
        return MemberDTO(id, firstName, lastName, email, phone, dateOfBirth, membershipDate, address, familyId, active)
    }
}
