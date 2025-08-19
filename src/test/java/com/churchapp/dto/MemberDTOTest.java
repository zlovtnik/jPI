package com.churchapp.dto;



import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MemberDTOTest {

    @Test
    void testMemberDTOBuilderAndGetters() {
        // Given
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        LocalDate membershipDate = LocalDate.now();

        // When
        MemberDTO memberDTO = MemberDTO.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("555-1234")
            .dateOfBirth(birthDate)
            .membershipDate(membershipDate)
            .familyId(1)
            .active(true)
            .build();

        // Then
        assertEquals(1, memberDTO.getId());
        assertEquals("John", memberDTO.getFirstName());
        assertEquals("Doe", memberDTO.getLastName());
        assertEquals("john.doe@example.com", memberDTO.getEmail());
        assertEquals("555-1234", memberDTO.getPhone());
        assertEquals(birthDate, memberDTO.getDateOfBirth());
        assertEquals(membershipDate, memberDTO.getMembershipDate());
        assertEquals(1, memberDTO.getFamilyId());
        assertTrue(memberDTO.getActive());
    }

    @Test
    void testMemberDTOSetters() {
        // Given
        MemberDTO memberDTO = new MemberDTO();
        LocalDate birthDate = LocalDate.of(1990, 1, 1);

        // When
        memberDTO.setId(1);
        memberDTO.setFirstName("John");
        memberDTO.setLastName("Doe");
        memberDTO.setEmail("john.doe@example.com");
        memberDTO.setPhone("555-1234");
        memberDTO.setDateOfBirth(birthDate);
        memberDTO.setFamilyId(1);
        memberDTO.setActive(true);

        // Then
        assertEquals(1, memberDTO.getId());
        assertEquals("John", memberDTO.getFirstName());
        assertEquals("Doe", memberDTO.getLastName());
        assertEquals("john.doe@example.com", memberDTO.getEmail());
        assertEquals("555-1234", memberDTO.getPhone());
        assertEquals(birthDate, memberDTO.getDateOfBirth());
        assertEquals(1, memberDTO.getFamilyId());
        assertTrue(memberDTO.getActive());
    }

    @Test
    void testMemberDTOEqualsAndHashCode() {
        // Given
        MemberDTO dto1 = MemberDTO.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        MemberDTO dto2 = MemberDTO.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        MemberDTO dto3 = MemberDTO.builder()
            .id(2)
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .build();

        // Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testMemberDTOToString() {
        // Given
        MemberDTO memberDTO = MemberDTO.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        // When
        String result = memberDTO.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Doe"));
    }
}
