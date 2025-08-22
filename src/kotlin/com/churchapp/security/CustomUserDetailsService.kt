package com.churchapp.security

import arrow.core.Option
import com.churchapp.entity.User
import com.churchapp.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")
    }

    // Additional method using Arrow Option for functional approach
    fun loadUserByUsernameOption(username: String): Option<User> = Option.fromNullable(userRepository.findByUsername(username))
}
