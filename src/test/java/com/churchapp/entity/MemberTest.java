package com.churchapp.entity;


import com.churchapp.entity.enums.RoleType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Test
    void testMemberBuilder() {
        // Given & When
        Member member = Member.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("555-1234")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .membershipDate(LocalDate.now())
            .active(true)
            .build();

        // Then
        assertEquals(1, member.getId());
        assertEquals("John", member.getFirstName());
        assertEquals("Doe", member.getLastName());
        assertEquals("john.doe@example.com", member.getEmail());
        assertEquals("555-1234", member.getPhone());
        assertEquals(LocalDate.of(1990, 1, 1), member.getDateOfBirth());
        assertTrue(member.getActive());
    }

    @Test
    void testMemberEqualsAndHashCode() {
        // Given
        Member member1 = Member.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        Member member2 = Member.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        Member member3 = Member.builder()
            .id(2)
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .build();

        // Then
        assertEquals(member1, member2);
        assertNotEquals(member1, member3);
        assertEquals(member1.hashCode(), member2.hashCode());
    }

    @Test
    void testMemberToString() {
        // Given
        Member member = Member.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        // When
        String result = member.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Doe"));
    }

    @Test
    void testMemberSettersAndGetters() {
        // Given
        Member member = new Member();
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        LocalDate membershipDate = LocalDate.now();

        // When
        member.setId(1);
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john.doe@example.com");
        member.setPhone("555-1234");
        member.setDateOfBirth(birthDate);
        member.setMembershipDate(membershipDate);
        member.setActive(true);

        // Then
        assertEquals(1, member.getId());
        assertEquals("John", member.getFirstName());
        assertEquals("Doe", member.getLastName());
        assertEquals("john.doe@example.com", member.getEmail());
        assertEquals("555-1234", member.getPhone());
        assertEquals(birthDate, member.getDateOfBirth());
        assertEquals(membershipDate, member.getMembershipDate());
        assertTrue(member.getActive());
    }
}
