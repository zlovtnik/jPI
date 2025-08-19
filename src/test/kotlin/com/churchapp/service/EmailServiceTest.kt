package com.churchapp.service

import arrow.core.Either
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.Unit

@ExtendWith(MockitoExtension::class)
class EmailServiceTest {
    private lateinit var emailService: EmailService

    @BeforeEach
    fun setUp() {
        emailService = EmailService("http://localhost:8080")
    }

    @Test
    fun `sendWelcomeEmail should send email successfully`() {
        // Given
        val email = "test@example.com"
        val name = "John Doe"

        // When
        val result = emailService.sendWelcomeEmail(email, name)

        // Then
        assertTrue(result.isRight())
    }

    @Test
    fun `sendPasswordResetEmail should send email successfully`() {
        // Given
        val email = "test@example.com"
        val token = "reset-token"

        // When
        val result: Either<EmailError, Unit> = emailService.sendPasswordResetEmail(email, token)

        // Then
        assertTrue(result.isRight())
    }

    @Test
    fun `sendWelcomeEmail should fail with invalid email`() {
        // Given
        val invalidEmail = "invalid-email"
        val name = "John Doe"

        // When
        val result = emailService.sendWelcomeEmail(invalidEmail, name)

        // Then
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is EmailError.InvalidRecipient)
    }

    @Test
    fun `sendPasswordResetEmail should fail with blank email`() {
        // Given
        val blankEmail = ""
        val token = "reset-token"

        // When
        val result = emailService.sendPasswordResetEmail(blankEmail, token)

        // Then
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is EmailError.InvalidRecipient)
    }
}
