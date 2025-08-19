package com.churchapp.service

import arrow.core.Either
import arrow.core.Option
import arrow.core.None
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import com.churchapp.entity.Member
import com.churchapp.repository.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class MemberService(
    private val memberRepository: MemberRepository
) {

    private val logger = LoggerFactory.getLogger(MemberService::class.java)

    // Using Arrow Either for error handling instead of exceptions
    fun findById(id: UUID): Either<MemberError, Member> = try {
        val optional = memberRepository.findById(id)
        if (optional.isPresent) {
            optional.get().right()
        } else {
            MemberError.NotFound(id).left()
        }
    } catch (e: Exception) {
        logger.error("Error finding member by id: $id", e)
        MemberError.DatabaseError(e.message ?: "Unknown database error").left()
    }

    // Using Arrow Option for nullable results
    fun findByEmailOption(email: String): Option<Member> = try {
        Option.fromNullable(memberRepository.findByEmail(email))
    } catch (e: Exception) {
        logger.error("Error finding member by email: $email", e)
        None
    }

    // Functional approach to member creation with validation
    fun createMember(member: Member): Either<MemberError, Member> {
        val validationResult = validateMember(member)
        return when (validationResult) {
            is Either.Left -> validationResult
            is Either.Right -> {
                val validMember = validationResult.value
                try {
                    memberRepository.save(validMember).right()
                } catch (e: Exception) {
                    logger.error("Error creating member", e)
                    MemberError.DatabaseError(e.message ?: "Failed to create member").left()
                }
            }
        }
    }

    // Functional validation using Arrow
    private fun validateMember(member: Member): Either<MemberError, Member> = when {
        member.firstName.isBlank() -> MemberError.ValidationError("First name cannot be blank").left()
        member.lastName.isBlank() -> MemberError.ValidationError("Last name cannot be blank").left()
        member.email.isBlank() -> MemberError.ValidationError("Email cannot be blank").left()
        !isValidEmail(member.email) -> MemberError.ValidationError("Invalid email format").left()
        memberRepository.existsByEmail(member.email) -> MemberError.ValidationError("Email already exists").left()
        else -> member.right()
    }

    // Get all active members using functional approach
    fun getActiveMembers(): Either<MemberError, List<Member>> = try {
        memberRepository.findByIsActive(true).right()
    } catch (e: Exception) {
        logger.error("Error retrieving active members", e)
        MemberError.DatabaseError(e.message ?: "Failed to retrieve members").left()
    }

    // Update member with functional validation
    fun updateMember(id: UUID, updatedMember: Member): Either<MemberError, Member> {
        val existingMemberResult = findById(id)
        return when (existingMemberResult) {
            is Either.Left -> existingMemberResult
            is Either.Right -> {
                val existingMember = existingMemberResult.value
                val validationResult = validateMember(updatedMember.copy(id = existingMember.id))
                when (validationResult) {
                    is Either.Left -> validationResult
                    is Either.Right -> {
                        val validMember = validationResult.value
                        try {
                            memberRepository.save(validMember).right()
                        } catch (e: Exception) {
                            logger.error("Error updating member: $id", e)
                            MemberError.DatabaseError(e.message ?: "Failed to update member").left()
                        }
                    }
                }
            }
        }
    }

    // Soft delete with functional approach
    fun deactivateMember(id: UUID): Either<MemberError, Member> {
        val memberResult = findById(id)
        return when (memberResult) {
            is Either.Left -> memberResult
            is Either.Right -> {
                val member = memberResult.value
                try {
                    memberRepository.save(member.copy(isActive = false)).right()
                } catch (e: Exception) {
                    logger.error("Error deactivating member: $id", e)
                    MemberError.DatabaseError(e.message ?: "Failed to deactivate member").left()
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean =
        email.matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"))
}

// Sealed class for type-safe error handling with Arrow
sealed class MemberError(val message: String) {
    data class NotFound(val id: UUID) : MemberError("Member with id $id not found")
    data class ValidationError(val details: String) : MemberError("Validation error: $details")
    data class DatabaseError(val details: String) : MemberError("Database error: $details")
}
