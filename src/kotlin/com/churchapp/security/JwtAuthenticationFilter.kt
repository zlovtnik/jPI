package com.churchapp.security

import arrow.core.getOrElse
import com.churchapp.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = jwtTokenService.extractTokenFromRequest(request)

        token?.let { jwt ->
            jwtTokenService.validateToken(jwt)
                .map { claims ->
                    val username = claims.subject
                    val user = userRepository.findByUsername(username)
                    user?.let {
                        val authentication = UsernamePasswordAuthenticationToken(
                            it,
                            null,
                            it.authorities
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
                .getOrElse {
                    // Log authentication failure but don't block the request
                    logger.debug("JWT authentication failed: $it")
                }
        }

        filterChain.doFilter(request, response)
    }
}
