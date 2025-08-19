package com.churchapp.service;

import com.churchapp.entity.Donation;
import com.churchapp.entity.Member;
import com.churchapp.entity.enums.DonationType;
import com.churchapp.repository.DonationRepository;
import io.vavr.control.Try;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DonationService donationService;

    private Donation testDonation;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        testDonation = Donation.builder()
            .id(1)
            .amount(new BigDecimal("100.00"))
            .donationType(DonationType.TITHE)
            .donationDate(LocalDate.now())
            .member(testMember)
            .taxDeductible(true)
            .build();
    }

    @Test
    void createDonation_ShouldReturnSuccessfulTry_WhenValidDonation() {
        // Given
        when(donationRepository.save(any(Donation.class))).thenReturn(testDonation);

        // When
        Try<Donation> result = donationService.createDonation(testDonation);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(testDonation, result.get());
        verify(donationRepository).save(testDonation);
        verify(eventPublisher).publishEvent(any(DonationService.DonationCreatedEvent.class));
    }

    @Test
    void createDonation_ShouldReturnFailure_WhenRepositoryThrowsException() {
        // Given
        when(donationRepository.save(any(Donation.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When
        Try<Donation> result = donationService.createDonation(testDonation);

        // Then
        assertTrue(result.isFailure());
        assertTrue(result.getCause() instanceof RuntimeException);
        assertEquals("Database error", result.getCause().getMessage());
    }

    @Test
    void updateDonation_ShouldReturnSuccessfulTry_WhenValidDonation() {
        // Given
        testDonation.setAmount(new BigDecimal("150.00"));
        when(donationRepository.save(any(Donation.class))).thenReturn(testDonation);

        // When
        Try<Donation> result = donationService.updateDonation(testDonation);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(testDonation, result.get());
        verify(donationRepository).save(testDonation);
    }

    @Test
    void updateDonation_ShouldReturnFailure_WhenRepositoryThrowsException() {
        // Given
        when(donationRepository.save(any(Donation.class)))
            .thenThrow(new RuntimeException("Update failed"));

        // When
        Try<Donation> result = donationService.updateDonation(testDonation);

        // Then
        assertTrue(result.isFailure());
        assertTrue(result.getCause() instanceof RuntimeException);
    }

    @Test
    void getAllDonations_ShouldReturnListOfDonations_WhenDonationsExist() {
        // Given
        java.util.List<Donation> donations = Arrays.asList(testDonation);
        when(donationRepository.findAll()).thenReturn(donations);

        // When
        List<Donation> result = donationService.getAllDonations();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testDonation, result.head());
    }

    @Test
    void getAllDonations_ShouldReturnEmptyList_WhenNoDonationsExist() {
        // Given
        when(donationRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Donation> result = donationService.getAllDonations();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllDonations_ShouldReturnEmptyList_WhenRepositoryThrowsException() {
        // Given
        when(donationRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        List<Donation> result = donationService.getAllDonations();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getDonationById_ShouldReturnSome_WhenDonationExists() {
        // Given
        when(donationRepository.findById(1)).thenReturn(Optional.of(testDonation));

        // When
        var result = donationService.getDonationById(1);

        // Then
        assertTrue(result.isDefined());
        assertEquals(testDonation, result.get());
    }

    @Test
    void getDonationById_ShouldReturnNone_WhenDonationDoesNotExist() {
        // Given
        when(donationRepository.findById(anyInt())).thenReturn(Optional.empty());

        // When
        var result = donationService.getDonationById(1);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getDonationsByMember_ShouldReturnDonations_WhenMemberHasDonations() {
        // Given
        java.util.List<Donation> donations = Arrays.asList(testDonation);
        when(donationRepository.findByMemberId(1)).thenReturn(donations);

        // When
        List<Donation> result = donationService.getDonationsByMember(1);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getTotalDonationsByMember_ShouldReturnCorrectSum() {
        // Given
        BigDecimal expectedTotal = new BigDecimal("350.00");
        when(donationRepository.sumDonationsByMemberId(1)).thenReturn(expectedTotal);

        // When
        var result = donationService.getTotalDonationsByMember(1);

        // Then
        assertTrue(result.isDefined());
        assertEquals(expectedTotal, result.get());
    }

    @Test
    void getDonationsByDateRange_ShouldReturnDonationsInRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        java.util.List<Donation> donations = Arrays.asList(testDonation);
        when(donationRepository.findByDonationDateBetween(startDate, endDate)).thenReturn(donations);

        // When
        List<Donation> result = donationService.getDonationsByDateRange(startDate, endDate);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void deleteDonation_ShouldReturnSuccessfulTry_WhenDonationExists() {
        // Given
        doNothing().when(donationRepository).deleteById(1);

        // When
        Try<Void> result = donationService.deleteDonation(1);

        // Then
        assertTrue(result.isSuccess());
        verify(donationRepository).deleteById(1);
    }

    @Test
    void deleteDonation_ShouldReturnFailure_WhenRepositoryThrowsException() {
        // Given
        doThrow(new RuntimeException("Delete failed")).when(donationRepository).deleteById(1);

        // When
        Try<Void> result = donationService.deleteDonation(1);

        // Then
        assertTrue(result.isFailure());
        assertTrue(result.getCause() instanceof RuntimeException);
    }
}
