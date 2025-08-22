package com.churchapp.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuditService {
    private val logger = LoggerFactory.getLogger(AuditService::class.java)

    // Functional audit logging with Either for error handling
    fun logMemberCreated(memberData: Any): Either<AuditError, AuditResult> =
        try {
            val message = "Member created: $memberData"
            logger.info(message)
            AuditResult(
                action = "MEMBER_CREATED",
                data = memberData.toString(),
                timestamp = LocalDateTime.now(),
                success = true,
            ).right()
        } catch (e: Exception) {
            logger.error("Error logging member creation", e)
            AuditError.LoggingFailed("Failed to log member creation: ${e.message}").left()
        }

    fun logDonationCreated(donationData: Any): Either<AuditError, AuditResult> =
        try {
            val message = "Donation created: $donationData"
            logger.info(message)
            AuditResult(
                action = "DONATION_CREATED",
                data = donationData.toString(),
                timestamp = LocalDateTime.now(),
                success = true,
            ).right()
        } catch (e: Exception) {
            logger.error("Error logging donation creation", e)
            AuditError.LoggingFailed("Failed to log donation creation: ${e.message}").left()
        }

    fun logEmailSent(emailData: Any): Either<AuditError, AuditResult> =
        try {
            val message = "Email sent: $emailData"
            logger.info(message)
            AuditResult(
                action = "EMAIL_SENT",
                data = emailData.toString(),
                timestamp = LocalDateTime.now(),
                success = true,
            ).right()
        } catch (e: Exception) {
            logger.error("Error logging email sending", e)
            AuditError.LoggingFailed("Failed to log email sending: ${e.message}").left()
        }

    fun logError(errorData: Any): Either<AuditError, AuditResult> =
        try {
            val message = "Error occurred: $errorData"
            logger.error(message)
            AuditResult(
                action = "ERROR_OCCURRED",
                data = errorData.toString(),
                timestamp = LocalDateTime.now(),
                success = false,
            ).right()
        } catch (e: Exception) {
            logger.error("Error logging error event", e)
            AuditError.LoggingFailed("Failed to log error: ${e.message}").left()
        }

    fun logUserAuthentication(
        username: String,
        success: Boolean,
    ): Either<AuditError, AuditResult> =
        try {
            val message = "User authentication: $username, success: $success"
            if (success) {
                logger.info(message)
            } else {
                logger.warn(message)
            }
            AuditResult(
                action = "USER_AUTHENTICATION",
                data = "Username: $username, Success: $success",
                timestamp = LocalDateTime.now(),
                success = success,
            ).right()
        } catch (e: Exception) {
            logger.error("Error logging authentication", e)
            AuditError.LoggingFailed("Failed to log authentication: ${e.message}").left()
        }
}

// Sealed class for audit errors
sealed class AuditError(val message: String) {
    data class LoggingFailed(val details: String) : AuditError("Logging failed: $details")
}

// Data class for audit results
data class AuditResult(
    val action: String,
    val data: String,
    val timestamp: LocalDateTime,
    val success: Boolean,
)
