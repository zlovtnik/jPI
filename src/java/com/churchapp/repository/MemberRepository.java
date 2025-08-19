package com.churchapp.repository;

import com.churchapp.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    
    Optional<Member> findByEmail(String email);
    
    List<Member> findByFamilyId(Integer familyId);
    
    List<Member> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
    
    @Query("SELECT m FROM Member m WHERE m.family.id = :familyId")
    List<Member> findMembersByFamilyId(@Param("familyId") Integer familyId);
    
    @Query("SELECT COUNT(m) FROM Member m WHERE m.family.id = :familyId")
    Long countMembersByFamilyId(@Param("familyId") Integer familyId);
    
    @Query("SELECT m FROM Member m WHERE m.membershipDate BETWEEN :startDate AND :endDate")
    List<Member> findMembersByMembershipDateRange(
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate);
}
