package com.churchapp.service;



import com.churchapp.entity.Member;
import com.churchapp.repository.MemberRepository;
import io.vavr.control.Try;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    
    @Mock
    private ProducerTemplate camelProducer;
    
    @InjectMocks
    private EmailService emailService;
    
    @Test
    void testSendWelcomeEmail() {
        // Given
        String email = "test@example.com";
        String name = "John Doe";
        
        // When
        Try<Void> result = emailService.sendWelcomeEmail(email, name);
        
        // Then
        assertTrue(result.isSuccess());
        verify(camelProducer).sendBody(eq("direct:sendEmail"), any());
    }
    
    @Test
    void testSendPasswordResetEmail() {
        // Given
        String email = "test@example.com";
        String token = "reset-token";
        
        // When
        Try<Void> result = emailService.sendPasswordResetEmail(email, token);
        
        // Then
        assertTrue(result.isSuccess());
        verify(camelProducer).sendBody(eq("direct:sendEmail"), any());
    }
}
