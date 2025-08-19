package com.churchapp.config

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.apache.camel.builder.RouteBuilder
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CamelConfig {

    private val logger = LoggerFactory.getLogger(CamelConfig::class.java)

    companion object {
        private const val MEMBER_CREATED_QUEUE = "spring-rabbitmq:member.created.queue"
        private const val DONATION_CREATED_QUEUE = "spring-rabbitmq:donation.created.queue"
        private const val EMAIL_NOTIFICATION_QUEUE = "spring-rabbitmq:email.notification.queue"
    }

    @Bean
    fun memberEventRoutes(): RouteBuilder = object : RouteBuilder() {
        override fun configure() {

            // Error handling - MUST be defined before any routes
            onException(Exception::class.java)
                .log("Error in Camel route: \${exception.message}")
                .to("direct:errorHandler")
                .handled(true)

            // Member created event routing
            from("direct:memberCreated")
                .log("Processing member created event: \${body}")
                .marshal().json()
                .to(MEMBER_CREATED_QUEUE)
                .to("direct:sendWelcomeEmail")

            // Member created queue consumer
            from(MEMBER_CREATED_QUEUE)
                .log("Received member created message: \${body}")
                .unmarshal().json()
                .to("bean:auditService?method=logMemberCreated")

            // Donation created event routing
            from("direct:donationCreated")
                .log("Processing donation created event: \${body}")
                .marshal().json()
                .to(DONATION_CREATED_QUEUE)
                .to("direct:sendDonationThankYou")

            // Email notification routing
            from("direct:sendWelcomeEmail")
                .log("Sending welcome email for: \${body}")
                .to(EMAIL_NOTIFICATION_QUEUE)

            // Donation thank you email routing
            from("direct:sendDonationThankYou")
                .log("Sending donation thank you email for: \${body}")
                .to(EMAIL_NOTIFICATION_QUEUE)

            // Email notification queue consumer
            from(EMAIL_NOTIFICATION_QUEUE)
                .log("Processing email notification: \${body}")
                .to("bean:emailService?method=sendEmail")

            // Error handler route
            from("direct:errorHandler")
                .log("Handling error: \${body}")
                .to("bean:auditService?method=logError")
        }
    }

    // Utility function using Arrow's Either for error handling
    fun <T> safeExecute(operation: () -> T): Either<Exception, T> = try {
        operation().right()
    } catch (e: Exception) {
        logger.error("Operation failed", e)
        e.left()
    }
}
