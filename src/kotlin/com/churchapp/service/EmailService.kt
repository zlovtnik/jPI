package com.churchapp.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.fx.coroutines.parMap
import com.churchapp.entity.Member
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class EmailService(
    @Value("\${app.publicBaseUrl}") private val publicBaseUrl: String
) {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    // Send email with functional error handling
    fun sendEmail(to: String, subject: String, body: String): Either<EmailError, EmailResult> = try {
        // Simulate email sending logic
        when {
            to.isBlank() -> EmailError.InvalidRecipient("Email address cannot be blank").left()
            subject.isBlank() -> EmailError.InvalidSubject("Subject cannot be blank").left()
            body.isBlank() -> EmailError.InvalidBody("Email body cannot be blank").left()
            !isValidEmail(to) -> EmailError.InvalidRecipient("Invalid email format: $to").left()
            else -> {
                logger.info("Sending email to: $to, Subject: $subject")
                // Simulate successful email sending
                EmailResult(to, subject, sent = true).right()
            }
        }
    } catch (e: Exception) {
        logger.error("Error sending email to: $to", e)
        EmailError.SendingFailed(e.message ?: "Unknown error occurred").left()
    }

    // Send welcome email to new member
    fun sendWelcomeEmail(member: Member): Either<EmailError, EmailResult> {
        val subject = "Welcome to Our Church Community!"
        val body = buildWelcomeEmailBody(member)
        return sendEmail(member.email, subject, body)
    }

    // Overloaded method for backwards compatibility with tests
    fun sendWelcomeEmail(email: String, name: String): Either<EmailError, Unit> {
        val subject = "Welcome to Our Church Community!"
        val body = "Dear $name,\n\nWelcome to our church community!"
        return sendEmail(email, subject, body).map { Unit }
    }

    // Send password reset email
    fun sendPasswordResetEmail(email: String, token: String): Either<EmailError, Unit> {
        val subject = "Password Reset Request"
        val encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8.name())
        val resetUrl = "$publicBaseUrl/reset?token=$encodedToken"
        val body = "Click here to reset your password: $resetUrl"
        return sendEmail(email, subject, body).map { Unit }
    }

    // Send donation thank you email
    fun sendDonationThankYouEmail(member: Member, amount: String): Either<EmailError, EmailResult> {
        val subject = "Thank You for Your Generous Donation"
        val body = buildDonationThankYouBody(member, amount)
        return sendEmail(member.email, subject, body)
    }

    // Bulk email sending with parallel processing using Arrow
    fun sendBulkEmails(emails: List<EmailRequest>): Either<EmailError, List<EmailResult>> = try {
        runBlocking {
            emails.parMap { request ->
                sendEmail(request.to, request.subject, request.body)
            }.let { results ->
                val failures = results.mapNotNull { it.leftOrNull() }
                if (failures.isNotEmpty()) {
                    EmailError.BulkSendingFailed(failures.map { it.message }).left()
                } else {
                    results.mapNotNull { it.getOrNull() }.right()
                }
            }
        }
    } catch (e: Exception) {
        logger.error("Error in bulk email sending", e)
        EmailError.SendingFailed(e.message ?: "Bulk email sending failed").left()
    }

    private fun buildWelcomeEmailBody(member: Member): String = """
        Dear ${member.firstName} ${member.lastName},
        
        Welcome to our church community! We're thrilled to have you as part of our family.
        
        Your membership was registered on ${member.membershipDate}.
        
        We look forward to seeing you at our services and events.
        
        Blessings,
        The Church Team
    """.trimIndent()

    private fun buildDonationThankYouBody(member: Member, amount: String): String = """
        Dear ${member.firstName} ${member.lastName},
        
        Thank you for your generous donation of $amount. Your contribution helps us continue 
        our mission and serve our community.
        
        May God bless you for your generosity.
        
        With gratitude,
        The Church Team
    """.trimIndent()

    private fun isValidEmail(email: String): Boolean =
        email.matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"))
}

// Sealed class for email errors
sealed class EmailError(val message: String) {
    data class InvalidRecipient(val details: String) : EmailError("Invalid recipient: $details")
    data class InvalidSubject(val details: String) : EmailError("Invalid subject: $details")
    data class InvalidBody(val details: String) : EmailError("Invalid body: $details")
    data class SendingFailed(val details: String) : EmailError("Email sending failed: $details")
    data class BulkSendingFailed(val errors: List<String>) : EmailError("Bulk sending failed: ${errors.joinToString(", ")}")
}

// Data classes for email operations
data class EmailRequest(
    val to: String,
    val subject: String,
    val body: String
)

data class EmailResult(
    val to: String,
    val subject: String,
    val sent: Boolean,
    val timestamp: java.time.LocalDateTime = java.time.LocalDateTime.now()
)
