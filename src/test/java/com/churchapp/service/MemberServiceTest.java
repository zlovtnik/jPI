package com.churchapp.service;

import com.churchapp.entity.Member;
import com.churchapp.repository.MemberRepository;
import arrow.core.Either;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private ProducerTemplate camelProducer;
    
    @InjectMocks
    private MemberService memberService;
    
    private Member testMember;
    
    @BeforeEach
    void setUp() {
        testMember = new Member(
            UUID.randomUUID(),
            "John",
            "Doe",
            "john.doe@example.com",
            "555-1234",
            LocalDate.of(1990, 1, 1),
            "123 Main St",
            LocalDate.now(),
            null, // baptismDate
            true, // isActive
            null, // family
            null, // user
            LocalDateTime.now(), // createdAt
            null  // updatedAt
        );
    }
    
    @Test
    void createMember_ShouldReturnSuccessfulTry_WhenValidMember() {
        // Given
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        
        // When
        Try<Member> result = memberService.createMember(testMember);
        
        // Then
        assertTrue(result.isSuccess());
        assertThat(result.get()).isEqualTo(testMember);
        verify(memberRepository).save(testMember);
        verify(camelProducer).sendBody("direct:memberCreated", testMember);
    }
    
    @Test
    void createMember_ShouldReturnFailure_WhenRepositoryThrowsException() {
        // Given
        when(memberRepository.save(any(Member.class))).thenThrow(new RuntimeException("Database error"));
        
        // When
        Try<Member> result = memberService.createMember(testMember);
        
        // Then
        assertTrue(result.isFailure());
        assertTrue(result.getCause() instanceof RuntimeException);
        assertEquals("Database error", result.getCause().getMessage());
    }
    
    @Test
    void getMemberById_ShouldReturnSome_WhenMemberExists() {
        // Given
        when(memberRepository.findById(1)).thenReturn(Optional.of(testMember));
        
        // When
        var result = memberService.getMemberById(1);
        
        // Then
        assertTrue(result.isDefined());
        assertEquals(testMember, result.get());
    }
    
    @Test
    void getMemberById_ShouldReturnNone_WhenMemberDoesNotExist() {
        // Given
        when(memberRepository.findById(1)).thenReturn(Optional.empty());
        
        // When
        var result = memberService.getMemberById(1);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void getMemberByEmail_ShouldReturnSome_WhenMemberExists() {
        // Given
        when(memberRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testMember));
        
        // When
        var result = memberService.getMemberByEmail("john.doe@example.com");
        
        // Then
        assertTrue(result.isDefined());
        assertEquals(testMember, result.get());
    }
    
    @Test
    void deleteMember_ShouldReturnSuccessfulTry_WhenMemberExists() {
        // Given
        doNothing().when(memberRepository).deleteById(1);
        
        // When
        Try<Void> result = memberService.deleteMember(1);
        
        // Then
        assertTrue(result.isSuccess());
        verify(memberRepository).deleteById(1);
    }
    
    @Test
    void deleteMember_ShouldReturnFailure_WhenRepositoryThrowsException() {
        // Given
        doThrow(new RuntimeException("Delete failed")).when(memberRepository).deleteById(1);

        // When
        Try<Void> result = memberService.deleteMember(1);

        // Then
        assertTrue(result.isFailure());
        assertTrue(result.getCause() instanceof RuntimeException);
        assertEquals("Delete failed", result.getCause().getMessage());
    }

    @Test
    void updateMember_ShouldReturnSuccessfulTry_WhenValidMember() {
        // Given
        testMember.setFirstName("Jane");
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // When
        Try<Member> result = memberService.updateMember(testMember);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Jane", result.get().getFirstName());
        verify(memberRepository).save(testMember);
    }

    @Test
    void updateMember_ShouldReturnFailure_WhenRepositoryThrowsException() {
        // Given
        when(memberRepository.save(any(Member.class))).thenThrow(new RuntimeException("Update failed"));

        // When
        Try<Member> result = memberService.updateMember(testMember);

        // Then
        assertTrue(result.isFailure());
        assertTrue(result.getCause() instanceof RuntimeException);
    }

    @Test
    void getAllMembers_ShouldReturnListOfMembers() {
        // Given
        java.util.List<Member> members = java.util.Arrays.asList(testMember);
        when(memberRepository.findAll()).thenReturn(members);

        // When
        var result = memberService.getAllMembers();

        // Then
        assertEquals(1, result.size());
        assertEquals(testMember, result.head());
    }

    @Test
    void searchMembers_ShouldReturnMatchingMembers() {
        // Given
        String searchTerm = "John";
        java.util.List<Member> members = java.util.Arrays.asList(testMember);
        when(memberRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(searchTerm, searchTerm))
            .thenReturn(members);

        // When
        var result = memberService.searchMembers(searchTerm);

        // Then
        assertEquals(1, result.size());
        assertEquals(testMember, result.head());
    }
}

