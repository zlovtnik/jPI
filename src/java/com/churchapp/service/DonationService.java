package com.churchapp.service;

import com.churchapp.entity.Donation;
import com.churchapp.entity.enums.DonationType;
import com.churchapp.repository.DonationRepository;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.collection.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DonationService {
    
    private final DonationRepository donationRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public Try<Donation> createDonation(Donation donation) {
        return Try.of(() -> {
            log.info("Creating new donation: ${} for member {}", 
                donation.getAmount(), donation.getMember().getId());
            Donation savedDonation = donationRepository.save(donation);
            
            // Publish event for Camel integration
            eventPublisher.publishEvent(new DonationCreatedEvent(savedDonation));
            
            return savedDonation;
        }).onFailure(throwable -> log.error("Failed to create donation", throwable));
    }
    
    public Try<Donation> updateDonation(Donation donation) {
        return Try.of(() -> {
            log.info("Updating donation with id: {}", donation.getId());
            return donationRepository.save(donation);
        }).onFailure(throwable -> log.error("Failed to update donation", throwable));
    }
    
    public List<Donation> getAllDonations() {
        return Try.of(() -> List.ofAll(donationRepository.findAll()))
            .onFailure(throwable -> log.error("Failed to retrieve all donations", throwable))
            .getOrElse(List.empty());
    }
    
    public Option<Donation> getDonationById(Integer id) {
        return Try.of(() -> Option.ofOptional(donationRepository.findById(id)))
            .onFailure(throwable -> log.error("Failed to find donation by id: {}", id, throwable))
            .getOrElse(Option.none());
    }
    
    public List<Donation> getDonationsByMember(Integer memberId) {
        return Try.of(() -> List.ofAll(donationRepository.findByMemberId(memberId)))
            .onFailure(throwable -> log.error("Failed to find donations by member id: {}", memberId, throwable))
            .getOrElse(List.empty());
    }
    
    public List<Donation> getDonationsByType(DonationType donationType) {
        return Try.of(() -> List.ofAll(donationRepository.findByDonationType(donationType)))
            .onFailure(throwable -> log.error("Failed to find donations by type: {}", donationType, throwable))
            .getOrElse(List.empty());
    }
    
    public List<Donation> getDonationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return Try.of(() -> List.ofAll(donationRepository.findByDonationDateBetween(startDate, endDate)))
            .onFailure(throwable -> log.error("Failed to find donations by date range", throwable))
            .getOrElse(List.empty());
    }
    
    public Option<BigDecimal> getTotalDonationsByMember(Integer memberId) {
        return Try.of(() -> Option.of(donationRepository.sumDonationsByMemberId(memberId)))
            .onFailure(throwable -> log.error("Failed to calculate total donations by member: {}", memberId, throwable))
            .getOrElse(Option.none());
    }
    
    public Option<BigDecimal> getTotalDonationsByType(DonationType donationType) {
        return Try.of(() -> Option.of(donationRepository.sumDonationsByType(donationType)))
            .onFailure(throwable -> log.error("Failed to calculate total donations by type: {}", donationType, throwable))
            .getOrElse(Option.none());
    }
    
    public Option<BigDecimal> getTotalDonationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return Try.of(() -> Option.of(donationRepository.sumDonationsByDateRange(startDate, endDate)))
            .onFailure(throwable -> log.error("Failed to calculate total donations by date range", throwable))
            .getOrElse(Option.none());
    }
    
    public Try<Void> deleteDonation(Integer id) {
        return Try.run(() -> {
            log.info("Deleting donation with id: {}", id);
            donationRepository.deleteById(id);
        }).onFailure(throwable -> log.error("Failed to delete donation with id: {}", id, throwable));
    }
    
    // Event class for Camel integration
    public static class DonationCreatedEvent {
        private final Donation donation;
        
        public DonationCreatedEvent(Donation donation) {
            this.donation = donation;
        }
        
        public Donation getDonation() {
            return donation;
        }
    }
}
