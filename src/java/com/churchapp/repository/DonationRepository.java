package com.churchapp.repository;

import com.churchapp.entity.Donation;
import com.churchapp.entity.enums.DonationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Integer> {
    
    List<Donation> findByMemberId(Integer memberId);
    
    List<Donation> findByDonationType(DonationType donationType);
    
    List<Donation> findByDonationDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.member.id = :memberId")
    BigDecimal sumDonationsByMemberId(@Param("memberId") Integer memberId);
    
    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.donationType = :donationType")
    BigDecimal sumDonationsByType(@Param("donationType") DonationType donationType);
    
    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.donationDate BETWEEN :startDate AND :endDate")
    BigDecimal sumDonationsByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT d FROM Donation d WHERE d.member.id = :memberId AND d.donationDate BETWEEN :startDate AND :endDate")
    List<Donation> findMemberDonationsByDateRange(
        @Param("memberId") Integer memberId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT d.donationType, SUM(d.amount) FROM Donation d WHERE d.donationDate BETWEEN :startDate AND :endDate GROUP BY d.donationType")
    List<Object[]> getDonationSummaryByType(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}
