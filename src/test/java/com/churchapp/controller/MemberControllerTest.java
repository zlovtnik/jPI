package com.churchapp.controller;

import com.churchapp.dto.MemberDTO;
import com.churchapp.entity.Member;
import com.churchapp.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.collection.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private Member testMember;
    private MemberDTO testMemberDTO;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("555-1234")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .membershipDate(LocalDate.now())
            .build();

        testMemberDTO = MemberDTO.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("555-1234")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .membershipDate(LocalDate.now())
            .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMember_ShouldReturnCreated_WhenValidMember() throws Exception {
        // Given
        when(memberService.createMember(any(Member.class))).thenReturn(Try.success(testMember));

        // When & Then
        mockMvc.perform(post("/api/members")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMemberDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMember_ShouldReturnInternalServerError_WhenServiceFails() throws Exception {
        // Given
        when(memberService.createMember(any(Member.class)))
                .thenReturn(Try.failure(new RuntimeException("Database error")));

        // When & Then
        mockMvc.perform(post("/api/members")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMemberDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void getMemberById_ShouldReturnMember_WhenMemberExists() throws Exception {
        // Given
        when(memberService.getMemberById(1)).thenReturn(Option.of(testMember));

        // When & Then
        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @WithMockUser
    void getMemberById_ShouldReturnNotFound_WhenMemberDoesNotExist() throws Exception {
        // Given
        when(memberService.getMemberById(anyInt())).thenReturn(Option.none());

        // When & Then
        mockMvc.perform(get("/api/members/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getAllMembers_ShouldReturnMembersList() throws Exception {
        // Given
        List<Member> members = List.of(testMember);
        when(memberService.getAllMembers()).thenReturn(members);

        // When & Then
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMember_ShouldReturnOk_WhenValidUpdate() throws Exception {
        // Given
        when(memberService.updateMember(any(Member.class))).thenReturn(Try.success(testMember));

        // When & Then
        mockMvc.perform(put("/api/members/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMemberDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMember_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        // Given
        when(memberService.deleteMember(1)).thenReturn(Try.success(null));

        // When & Then
        mockMvc.perform(delete("/api/members/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMember_ShouldReturnInternalServerError_WhenServiceFails() throws Exception {
        // Given
        when(memberService.deleteMember(1)).thenReturn(Try.failure(new RuntimeException("Delete failed")));

        // When & Then
        mockMvc.perform(delete("/api/members/1")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void searchMembers_ShouldReturnMatchingMembers() throws Exception {
        // Given
        List<Member> members = List.of(testMember);
        when(memberService.searchMembers("John")).thenReturn(members);

        // When & Then
        mockMvc.perform(get("/api/members/search")
                .param("query", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpected(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void createMember_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMemberDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void createMember_ShouldReturnForbidden_WhenInsufficientRole() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/members")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMemberDTO)))
                .andExpect(status().isForbidden());
    }
}
