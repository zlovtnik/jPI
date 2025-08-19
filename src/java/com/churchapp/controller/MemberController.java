package com.churchapp.controller;

import com.churchapp.dto.MemberDTO;
import com.churchapp.entity.Member;
import com.churchapp.service.MemberService;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    
    private final MemberService memberService;
    
    @PostMapping
    public ResponseEntity<?> createMember(@Valid @RequestBody MemberDTO memberDTO) {
        return convertToEntity(memberDTO)
            .flatMap(memberService::createMember)
            .map(this::convertToDTO)
            .fold(
                throwable -> {
                    log.error("Failed to create member", throwable);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to create member: " + throwable.getMessage());
                },
                memberDto -> ResponseEntity.status(HttpStatus.CREATED).body(memberDto)
            );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getMemberById(@PathVariable Integer id) {
        return memberService.getMemberById(id)
            .map(this::convertToDTO)
            .map(memberDto -> ResponseEntity.ok(memberDto))
            .getOrElse(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<MemberDTO>> getAllMembers() {
        List<MemberDTO> members = memberService.getAllMembers()
            .map(this::convertToDTO)
            .asJava();
        return ResponseEntity.ok(members);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<MemberDTO>> searchMembers(@RequestParam String term) {
        List<MemberDTO> members = memberService.searchMembers(term)
            .map(this::convertToDTO)
            .asJava();
        return ResponseEntity.ok(members);
    }
    
    @GetMapping("/family/{familyId}")
    public ResponseEntity<List<MemberDTO>> getMembersByFamily(@PathVariable Integer familyId) {
        List<MemberDTO> members = memberService.getMembersByFamily(familyId)
            .map(this::convertToDTO)
            .asJava();
        return ResponseEntity.ok(members);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getMemberByEmail(@PathVariable String email) {
        return memberService.getMemberByEmail(email)
            .map(this::convertToDTO)
            .map(memberDto -> ResponseEntity.ok(memberDto))
            .getOrElse(() -> ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Integer id, @Valid @RequestBody MemberDTO memberDTO) {
        memberDTO.setId(id);
        return convertToEntity(memberDTO)
            .flatMap(memberService::updateMember)
            .map(this::convertToDTO)
            .fold(
                throwable -> {
                    log.error("Failed to update member", throwable);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to update member: " + throwable.getMessage());
                },
                memberDto -> ResponseEntity.ok(memberDto)
            );
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Integer id) {
        return memberService.deleteMember(id)
            .fold(
                throwable -> {
                    log.error("Failed to delete member", throwable);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete member: " + throwable.getMessage());
                },
                success -> ResponseEntity.noContent().build()
            );
    }
    
    @GetMapping("/membership-range")
    public ResponseEntity<List<MemberDTO>> getMembersByMembershipDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<MemberDTO> members = memberService.getMembersByMembershipDateRange(startDate, endDate)
            .map(this::convertToDTO)
            .asJava();
        return ResponseEntity.ok(members);
    }
    
    private Try<Member> convertToEntity(MemberDTO dto) {
        return Try.of(() -> Member.builder()
            .id(dto.getId())
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .dateOfBirth(dto.getDateOfBirth())
            .gender(dto.getGender())
            .maritalStatus(dto.getMaritalStatus())
            .membershipDate(dto.getMembershipDate())
            .baptismDate(dto.getBaptismDate())
            .occupation(dto.getOccupation())
            .emergencyContact(dto.getEmergencyContact())
            .notes(dto.getNotes())
            .build());
    }
    
    private MemberDTO convertToDTO(Member member) {
        return MemberDTO.builder()
            .id(member.getId())
            .firstName(member.getFirstName())
            .lastName(member.getLastName())
            .email(member.getEmail())
            .phone(member.getPhone())
            .dateOfBirth(member.getDateOfBirth())
            .gender(member.getGender())
            .maritalStatus(member.getMaritalStatus())
            .membershipDate(member.getMembershipDate())
            .baptismDate(member.getBaptismDate())
            .occupation(member.getOccupation())
            .emergencyContact(member.getEmergencyContact())
            .notes(member.getNotes())
            .familyId(Option.of(member.getFamily()).map(family -> family.getId()).getOrNull())
            .familyName(Option.of(member.getFamily()).map(family -> family.getFamilyName()).getOrNull())
            .build();
    }
}
