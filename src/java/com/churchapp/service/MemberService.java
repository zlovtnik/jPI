package com.churchapp.service;

import com.churchapp.entity.Member;
import com.churchapp.repository.MemberRepository;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.collection.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final ProducerTemplate camelProducer;
    
    public Try<Member> createMember(Member member) {
        return Try.of(() -> {
            log.info("Creating new member: {} {}", member.getFirstName(), member.getLastName());
            Member savedMember = memberRepository.save(member);
            
            // Send async notification via RabbitMQ
            camelProducer.sendBody("direct:memberCreated", savedMember);
            
            return savedMember;
        }).onFailure(throwable -> log.error("Failed to create member", throwable));
    }
    
    public Try<Member> updateMember(Member member) {
        return Try.of(() -> {
            log.info("Updating member with id: {}", member.getId());
            return memberRepository.save(member);
        }).onFailure(throwable -> log.error("Failed to update member", throwable));
    }
    
    public List<Member> getAllMembers() {
        return Try.of(() -> List.ofAll(memberRepository.findAll()))
            .onFailure(throwable -> log.error("Failed to retrieve all members", throwable))
            .getOrElse(List.empty());
    }
    
    public Option<Member> getMemberById(Integer id) {
        return Try.of(() -> Option.ofOptional(memberRepository.findById(id)))
            .onFailure(throwable -> log.error("Failed to find member by id: {}", id, throwable))
            .getOrElse(Option.none());
    }
    
    public Option<Member> getMemberByEmail(String email) {
        return Try.of(() -> Option.ofOptional(memberRepository.findByEmail(email)))
            .onFailure(throwable -> log.error("Failed to find member by email: {}", email, throwable))
            .getOrElse(Option.none());
    }
    
    public List<Member> getMembersByFamily(Integer familyId) {
        return Try.of(() -> List.ofAll(memberRepository.findByFamilyId(familyId)))
            .onFailure(throwable -> log.error("Failed to find members by family id: {}", familyId, throwable))
            .getOrElse(List.empty());
    }
    
    public List<Member> searchMembers(String searchTerm) {
        return Try.of(() -> List.ofAll(memberRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(searchTerm, searchTerm)))
            .onFailure(throwable -> log.error("Failed to search members with term: {}", searchTerm, throwable))
            .getOrElse(List.empty());
    }
    
    public List<Member> getMembersByMembershipDateRange(LocalDate startDate, LocalDate endDate) {
        return Try.of(() -> List.ofAll(memberRepository
            .findMembersByMembershipDateRange(startDate, endDate)))
            .onFailure(throwable -> log.error("Failed to find members by membership date range", throwable))
            .getOrElse(List.empty());
    }
    
    public Try<Void> deleteMember(Integer id) {
        return Try.run(() -> {
            log.info("Deleting member with id: {}", id);
            memberRepository.deleteById(id);
        }).onFailure(throwable -> log.error("Failed to delete member with id: {}", id, throwable));
    }
    
    public List<Member> getActiveMembers() {
        return getAllMembers()
            .filter(member -> member.getMembershipDate() != null);
    }
    
    public List<Member> getMembersByAge(int minAge, int maxAge) {
        return getAllMembers()
            .filter(member -> Option.of(member.getDateOfBirth())
                .map(birthDate -> {
                    int age = LocalDate.now().getYear() - birthDate.getYear();
                    return age >= minAge && age <= maxAge;
                })
                .getOrElse(false));
    }
}
