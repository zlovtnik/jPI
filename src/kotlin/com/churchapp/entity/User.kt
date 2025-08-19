package com.churchapp.entity

import arrow.core.Option
import arrow.core.some
import arrow.core.none
import com.churchapp.entity.enums.RoleType
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(unique = true, nullable = false)
    private val username: String,

    @Column(nullable = false)
    private val password: String,

    @Column(unique = true, nullable = false)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: RoleType = RoleType.MEMBER,

    @Column(nullable = false)
    private var enabled: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = enabled

    // Arrow Option for safe nullable access
    fun getIdOption(): Option<UUID> = id?.some() ?: none()

    fun getUpdatedAtOption(): Option<LocalDateTime> = updatedAt?.some() ?: none()

    fun setActive(active: Boolean) {
        enabled = active
    }

    companion object {
        @JvmStatic
        fun builder(): UserBuilder {
            return UserBuilder()
        }
    }
}

class UserBuilder {
    private var id: UUID? = null
    private var username: String = ""
    private var password: String = ""
    private var email: String = ""
    private var role: RoleType = RoleType.MEMBER
    private var enabled: Boolean = true
    private var createdAt: LocalDateTime = LocalDateTime.now()
    private var updatedAt: LocalDateTime? = null

    fun id(id: UUID?) = apply { this.id = id }
    fun username(username: String) = apply { this.username = username }
    fun password(password: String) = apply { this.password = password }
    fun email(email: String) = apply { this.email = email }
    fun role(role: RoleType) = apply { this.role = role }
    fun enabled(enabled: Boolean) = apply { this.enabled = enabled }
    fun createdAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
    fun updatedAt(updatedAt: LocalDateTime?) = apply { this.updatedAt = updatedAt }

    fun build(): User {
        return User(id, username, password, email, role, enabled, createdAt, updatedAt)
    }
}
