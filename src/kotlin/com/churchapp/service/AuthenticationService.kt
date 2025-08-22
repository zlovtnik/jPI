package com.churchapp.service

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import com.churchapp.entity.User
import com.churchapp.entity.enums.RoleType
import com.churchapp.repository.UserRepository
import com.churchapp.security.JwtTokenService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenService: JwtTokenService,
) {
    private val logger = LoggerFactory.getLogger(AuthenticationService::class.java)

    // Authenticate user with functional error handling
    fun authenticate(
        username: String,
        password: String,
    ): Either<AuthError, AuthResult> {
        val credentialsResult = validateCredentials(username, password)
        return when (credentialsResult) {
            is Either.Left -> credentialsResult
            is Either.Right -> {
                val credentials = credentialsResult.value
                val userOption = findUserByUsername(credentials.username)
                when (userOption) {
                    is None -> AuthError.InvalidCredentials("User not found").left()
                    is Some -> {
                        val user = userOption.value
                        if (passwordEncoder.matches(credentials.password, user.password)) {
                            val tokenResult = generateToken(user)
                            when (tokenResult) {
                                is Either.Left -> tokenResult
                                is Either.Right -> AuthResult(user, tokenResult.value).right()
                            }
                        } else {
                            AuthError.InvalidCredentials("Invalid password").left()
                        }
                    }
                }
            }
        }
    }

    // Register new user with validation
    fun registerUser(
        username: String,
        email: String,
        password: String,
        role: RoleType = RoleType.MEMBER,
    ): Either<AuthError, User> {
        val validationResult = validateRegistration(username, email, password)
        return when (validationResult) {
            is Either.Left -> validationResult
            is Either.Right -> {
                val (_, _, _, normUsername, normEmail, normPassword) = validationResult.value

                if (userRepository.existsByUsername(normUsername)) {
                    AuthError.UserAlreadyExists("Username already exists").left()
                } else if (userRepository.existsByEmail(normEmail)) {
                    AuthError.UserAlreadyExists("Email already exists").left()
                } else {
                    try {
                        val encodedPassword = passwordEncoder.encode(normPassword)
                        val user =
                            User(
                                username = normUsername,
                                password = encodedPassword,
                                email = normEmail,
                                role = role,
                                createdAt = LocalDateTime.now(),
                            )
                        userRepository.save(user).right()
                    } catch (e: org.springframework.dao.DataIntegrityViolationException) {
                        // Handle race condition where username or email was taken after our check
                        logger.warn("Unique constraint violation registering user: $username", e)
                        AuthError.UserAlreadyExists("Username or email already exists").left()
                    } catch (e: Exception) {
                        logger.error("Error registering user: $username", e)
                        AuthError.RegistrationFailed(e.message ?: "Unknown error").left()
                    }
                }
            }
        }
    }

    // Generate JWT token
    fun generateToken(user: User): Either<AuthError, String> =
        jwtTokenService.generateToken(user)
            .mapLeft<AuthError> { AuthError.TokenGenerationFailed(it) }

    // Get user from token
    fun getUserFromToken(token: String): Either<AuthError, User> {
        val tokenValidationResult = jwtTokenService.validateToken(token)
        return when (tokenValidationResult) {
            is Either.Left -> AuthError.InvalidToken(tokenValidationResult.value).left()
            is Either.Right -> {
                val claims = tokenValidationResult.value
                val username = claims.subject
                val userOption = findUserByUsername(username)
                when (userOption) {
                    is None -> AuthError.UserNotFound("User not found: $username").left()
                    is Some -> userOption.value.right()
                }
            }
        }
    }

    // Find user by username with Option
    private fun findUserByUsername(username: String): Option<User> =
        try {
            Option.fromNullable(userRepository.findByUsername(username))
        } catch (e: Exception) {
            logger.error("Error finding user by username: $username", e)
            None
        }

    // Validation functions using Arrow
    private fun validateCredentials(
        username: String,
        password: String,
    ): Either<AuthError, Credentials> =
        when {
            username.isBlank() -> AuthError.ValidationError("Username cannot be blank").left()
            password.isBlank() -> AuthError.ValidationError("Password cannot be blank").left()
            else -> Credentials(username, password).right()
        }

    private fun validateRegistration(
        username: String,
        email: String,
        password: String,
    ): Either<AuthError, ValidationResult> {
        val normalizedUsername = username.trim()
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()

        return when {
            normalizedUsername.isBlank() -> AuthError.ValidationError("Username cannot be blank").left()
            normalizedUsername.length < 3 -> AuthError.ValidationError("Username must be at least 3 characters").left()
            normalizedEmail.isBlank() -> AuthError.ValidationError("Email cannot be blank").left()
            !isValidEmail(normalizedEmail) -> AuthError.ValidationError("Invalid email format").left()
            normalizedPassword.isBlank() -> AuthError.ValidationError("Password cannot be blank").left()
            normalizedPassword.length < 6 -> AuthError.ValidationError("Password must be at least 6 characters").left()
            else ->
                ValidationResult(
                    username = normalizedUsername,
                    email = normalizedEmail,
                    password = normalizedPassword,
                ).right()
        }
    }

    private fun isValidEmail(email: String): Boolean = email.matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"))
}

// Sealed class for authentication errors
sealed class AuthError(val message: String) {
    data class InvalidCredentials(val details: String) : AuthError("Invalid credentials: $details")

    data class UserAlreadyExists(val details: String) : AuthError("User already exists: $details")

    data class UserNotFound(val details: String) : AuthError("User not found: $details")

    data class ValidationError(val details: String) : AuthError("Validation error: $details")

    data class RegistrationFailed(val details: String) : AuthError("Registration failed: $details")

    data class TokenGenerationFailed(val details: String) : AuthError("Token generation failed: $details")

    data class InvalidToken(val details: String) : AuthError("Invalid token: $details")
}

// Data classes for authentication
data class Credentials(val username: String, val password: String)

data class ValidationResult(
    val username: String,
    val email: String,
    val password: String,
    val normalizedUsername: String = username.trim().lowercase(),
    val normalizedEmail: String = email.trim().lowercase(),
    val normalizedPassword: String = password.trim(),
)

data class AuthResult(val user: User, val token: String)
