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
class User(
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

    // Custom equals/hashCode based on stable identity (database id when non-null, reference equality otherwise)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        
        // If both objects have non-null ids, compare by id (stable database identity)
        return if (id != null && other.id != null) {
            id == other.id
        } else {
            // Fall back to reference equality for transient objects
            this === other
        }
    }

    override fun hashCode(): Int {
        // Use id for hash when available (stable), otherwise use System.identityHashCode for reference-based hash
        return id?.hashCode() ?: System.identityHashCode(this)
    }

    override fun toString(): String {
        return "User(id=$id, username='$username', email='$email', role=$role, enabled=$enabled, createdAt=$createdAt, updatedAt=$updatedAt)"
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
    private var username: String? = null
    private var password: String? = null
    private var email: String? = null
    private var role: RoleType = RoleType.MEMBER
    private var enabled: Boolean = true
    private var createdAt: LocalDateTime? = null
    private var updatedAt: LocalDateTime? = null

    fun id(id: UUID?) = apply { this.id = id }
    fun username(username: String?) = apply { this.username = username }
    fun password(password: String?) = apply { this.password = password }
    fun email(email: String?) = apply { this.email = email }
    fun role(role: RoleType) = apply { this.role = role }
    fun enabled(enabled: Boolean) = apply { this.enabled = enabled }
    fun createdAt(createdAt: LocalDateTime?) = apply { this.createdAt = createdAt }
    fun updatedAt(updatedAt: LocalDateTime?) = apply { this.updatedAt = updatedAt }

    fun build(): User {
        // Validate required fields
        val missingFields = mutableListOf<String>()
        
        if (username.isNullOrBlank()) {
            missingFields.add("username")
        }
        if (password.isNullOrBlank()) {
            missingFields.add("password")
        }
        if (email.isNullOrBlank()) {
            missingFields.add("email")
        }
        
        if (missingFields.isNotEmpty()) {
            throw IllegalStateException("Missing required fields: ${missingFields.joinToString(", ")}")
        }
        
        // Set createdAt to now if not explicitly provided
        val finalCreatedAt = createdAt ?: LocalDateTime.now()
        
        return User(id, username!!, password!!, email!!, role, enabled, finalCreatedAt, updatedAt)
    }
}
