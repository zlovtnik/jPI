package com.churchapp.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CamelConfig {
    
    private static final String MEMBER_CREATED_QUEUE = "spring-rabbitmq:member.created.queue";
    private static final String DONATION_CREATED_QUEUE = "spring-rabbitmq:donation.created.queue";
    private static final String EMAIL_NOTIFICATION_QUEUE = "spring-rabbitmq:email.notification.queue";
    
    @Bean
    public RouteBuilder memberEventRoutes() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                
                // Error handling - MUST be defined before any routes
                onException(Exception.class)
                    .log("Error in Camel route: ${exception.message}")
                    .to("direct:errorHandler")
                    .handled(true);
                
                // Member created event routing
                from("direct:memberCreated")
                    .log("Processing member created event: ${body}")
                    .marshal().json()
                    .to(MEMBER_CREATED_QUEUE)
                    .to("direct:sendWelcomeEmail");
                
                // Member created queue consumer
                from(MEMBER_CREATED_QUEUE)
                    .log("Received member created message: ${body}")
                    .unmarshal().json()
                    .to("bean:auditService?method=logMemberCreated");
                
                // Donation created event routing
                from("direct:donationCreated")
                    .log("Processing donation created event: ${body}")
                    .marshal().json()
                    .to(DONATION_CREATED_QUEUE)
                    .to("direct:sendDonationThankYou");
                
                // Email notification routing
                from("direct:sendWelcomeEmail")
                    .log("Sending welcome email for: ${body}")
                    .to("bean:emailService?method=sendWelcomeEmail");
                
                from("direct:sendDonationThankYou")
                    .log("Sending donation thank you for: ${body}")
                    .to("bean:emailService?method=sendDonationThankYou");
                
                from("direct:errorHandler")
                    .log("Handling error: ${body}")
                    .to("bean:auditService?method=logError");
            }
        };
    }
}
