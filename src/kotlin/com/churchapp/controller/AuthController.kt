package com.churchapp.controller

import com.churchapp.entity.enums.RoleType
import com.churchapp.service.AuthError
import com.churchapp.service.AuthenticationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val authenticationService: AuthenticationService,
) {
    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequest,
    ): ResponseEntity<Any> =
        authenticationService.authenticate(loginRequest.username, loginRequest.password).fold(
            ifLeft = { error -> handleAuthError(error) },
            ifRight = { authResult ->
                ResponseEntity.ok(
                    LoginResponse(
                        token = authResult.token,
                        username = authResult.user.username,
                        role = authResult.user.role.name,
                        email = authResult.user.email,
                    ),
                )
            },
        )

    @PostMapping("/register")
    fun register(
        @RequestBody registerRequest: RegisterRequest,
    ): ResponseEntity<Any> =
        authenticationService.registerUser(
            username = registerRequest.username,
            email = registerRequest.email,
            password = registerRequest.password,
            role = registerRequest.role ?: RoleType.MEMBER,
        ).fold(
            ifLeft = { error -> handleAuthError(error) },
            ifRight = { user ->
                ResponseEntity.status(HttpStatus.CREATED).body(
                    RegisterResponse(
                        id = user.id.toString(),
                        username = user.username,
                        email = user.email,
                        role = user.role.name,
                    ),
                )
            },
        )

    @PostMapping("/validate")
    fun validateToken(
        @RequestBody tokenRequest: TokenRequest,
    ): ResponseEntity<Any> =
        authenticationService.getUserFromToken(tokenRequest.token).fold(
            ifLeft = { error -> handleAuthError(error) },
            ifRight = { user ->
                ResponseEntity.ok(
                    UserInfo(
                        id = user.id.toString(),
                        username = user.username,
                        email = user.email,
                        role = user.role.name,
                    ),
                )
            },
        )

    // Functional error handling using Arrow Either
    private fun handleAuthError(error: AuthError): ResponseEntity<Any> =
        when (error) {
            is AuthError.InvalidCredentials ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse(error.message, "INVALID_CREDENTIALS"))
            is AuthError.UserAlreadyExists ->
                ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse(error.message, "USER_ALREADY_EXISTS"))
            is AuthError.UserNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse(error.message, "USER_NOT_FOUND"))
            is AuthError.ValidationError ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse(error.message, "VALIDATION_ERROR"))
            is AuthError.RegistrationFailed ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse(error.message, "REGISTRATION_FAILED"))
            is AuthError.TokenGenerationFailed ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse(error.message, "TOKEN_GENERATION_FAILED"))
            is AuthError.InvalidToken ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse(error.message, "INVALID_TOKEN"))
        }
}

// Request/Response DTOs
data class LoginRequest(
    val username: String,
    val password: String,
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: RoleType? = null,
)

data class TokenRequest(
    val token: String,
)

data class LoginResponse(
    val token: String,
    val username: String,
    val role: String,
    val email: String,
)

data class RegisterResponse(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
)

data class UserInfo(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
)
