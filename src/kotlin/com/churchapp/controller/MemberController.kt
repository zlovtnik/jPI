package com.churchapp.controller

import arrow.core.Either
import arrow.core.getOrElse
import com.churchapp.entity.Member
import com.churchapp.service.MemberService
import com.churchapp.service.MemberError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = ["*"])
class MemberController(
    private val memberService: MemberService
) {

    @GetMapping("/{id}")
    fun getMemberById(@PathVariable id: UUID): ResponseEntity<Any> =
        memberService.findById(id).fold(
            ifLeft = { error -> handleMemberError(error) },
            ifRight = { member -> ResponseEntity.ok(member) }
        )

    @GetMapping
    fun getAllActiveMembers(): ResponseEntity<Any> =
        memberService.getActiveMembers().fold(
            ifLeft = { error -> handleMemberError(error) },
            ifRight = { members -> ResponseEntity.ok(members) }
        )

    @PostMapping
    fun createMember(@RequestBody member: Member): ResponseEntity<Any> =
        memberService.createMember(member).fold(
            ifLeft = { error -> handleMemberError(error) },
            ifRight = { createdMember -> ResponseEntity.status(HttpStatus.CREATED).body(createdMember) }
        )

    @PutMapping("/{id}")
    fun updateMember(
        @PathVariable id: UUID,
        @RequestBody member: Member
    ): ResponseEntity<Any> =
        memberService.updateMember(id, member).fold(
            ifLeft = { error -> handleMemberError(error) },
            ifRight = { updatedMember -> ResponseEntity.ok(updatedMember) }
        )

    @DeleteMapping("/{id}")
    fun deactivateMember(@PathVariable id: UUID): ResponseEntity<Any> =
        memberService.deactivateMember(id).fold(
            ifLeft = { error -> handleMemberError(error) },
            ifRight = { deactivatedMember -> ResponseEntity.ok(deactivatedMember) }
        )

    @GetMapping("/email/{email}")
    fun getMemberByEmail(@PathVariable email: String): ResponseEntity<Any> =
        memberService.findByEmailOption(email).fold(
            ifEmpty = { ResponseEntity.notFound().build<Any>() },
            ifSome = { member -> ResponseEntity.ok(member) }
        )

    // Functional error handling using Arrow Either
    private fun handleMemberError(error: MemberError): ResponseEntity<Any> = when (error) {
        is MemberError.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(error.message, "MEMBER_NOT_FOUND"))
        is MemberError.ValidationError -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(error.message, "VALIDATION_ERROR"))
        is MemberError.DatabaseError -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(error.message, "DATABASE_ERROR"))
    }
}

data class ErrorResponse(
    val message: String,
    val code: String,
    val timestamp: java.time.LocalDateTime = java.time.LocalDateTime.now()
)
