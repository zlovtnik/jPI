package com.churchapp.security

import com.churchapp.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class ChurchUserPrincipal(private val user: User) : UserDetails {
    
    override fun getAuthorities(): Collection<GrantedAuthority> = user.authorities
    
    override fun getPassword(): String = user.password
    
    override fun getUsername(): String = user.username
    
    override fun isAccountNonExpired(): Boolean = user.isAccountNonExpired
    
    override fun isAccountNonLocked(): Boolean = user.isAccountNonLocked
    
    override fun isCredentialsNonExpired(): Boolean = user.isCredentialsNonExpired
    
    override fun isEnabled(): Boolean = user.isEnabled
    
    fun getUser(): User = user
    
    fun getId() = user.id
    
    fun getEmail() = user.email
    
    fun getRole() = user.role
}
