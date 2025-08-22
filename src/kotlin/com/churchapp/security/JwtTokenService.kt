package com.churchapp.security

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.churchapp.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtTokenService(
    @Value("\${jwt.secret:defaultSecretKeyThatShouldBeChangedInProduction}")
    private val jwtSecret: String,
    @Value("\${jwt.expiration:86400}")
    private val jwtExpirationInSeconds: Long,
) {
    private val logger = LoggerFactory.getLogger(JwtTokenService::class.java)
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    fun generateToken(user: User): Either<String, String> =
        try {
            val now = Date()
            val expiryDate = Date(now.time + jwtExpirationInSeconds * 1000)

            val token =
                Jwts.builder()
                    .subject(user.username)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .claim("userId", user.id.toString())
                    .claim("role", user.role.name)
                    .signWith(secretKey)
                    .compact()

            token.right()
        } catch (e: Exception) {
            logger.error("Error generating token for user: ${user.username}", e)
            "Token generation failed: ${e.message}".left()
        }

    fun validateToken(token: String): Either<String, Claims> =
        try {
            val claims =
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload

            claims.right()
        } catch (e: Exception) {
            logger.debug("Token validation failed", e)
            "Token validation failed: ${e.message}".left()
        }

    fun extractTokenFromRequest(request: jakarta.servlet.http.HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken?.startsWith("Bearer ") == true) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
