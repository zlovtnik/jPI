package com.churchapp.entity.enums

enum class RoleType(val description: String) {
    ADMIN("Administrator with full access"),
    PASTOR("Church pastor with elevated privileges"),
    MEMBER("Regular church member"),
    VOLUNTEER("Church volunteer"),
    VISITOR("Church visitor"),
}
