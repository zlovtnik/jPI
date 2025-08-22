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
        // Shared exchange for churchapp events
        private const val EVENTS_EXCHANGE =
            "spring-rabbitmq:churchapp.events?autoDeclare=true"

        // Producers
        private const val MEMBER_CREATED_PRODUCER =
            "$EVENTS_EXCHANGE&routingKey=member.created"
        private const val DONATION_CREATED_PRODUCER =
            "$EVENTS_EXCHANGE&routingKey=donation.created"
        private const val EMAIL_NOTIFICATION_PRODUCER =
            "$EVENTS_EXCHANGE&routingKey=email.notification"

        // Consumers
        private const val MEMBER_CREATED_CONSUMER =
            "$EVENTS_EXCHANGE&queues=member.created.queue"
        private const val EMAIL_NOTIFICATION_CONSUMER =
            "$EVENTS_EXCHANGE&queues=email.notification.queue"
    }

    @Bean
    fun memberEventRoutes(): RouteBuilder =
        object : RouteBuilder() {
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
                    .to(MEMBER_CREATED_PRODUCER)
                    .to("direct:sendWelcomeEmail")

                // Member created queue consumer
                from(MEMBER_CREATED_CONSUMER)
                    .log("Received member created message: \${body}")
                    .unmarshal().json()
                    .to("bean:auditService?method=logMemberCreated")

                // Donation created event routing
                from("direct:donationCreated")
                    .log("Processing donation created event: \${body}")
                    .marshal().json()
                    .to(DONATION_CREATED_PRODUCER)
                    .to("direct:sendDonationThankYou")

                // Email notification routing
                from("direct:sendWelcomeEmail")
                    .log("Sending welcome email for: \${body}")
                    .to(EMAIL_NOTIFICATION_PRODUCER)

                // Donation thank you email routing
                from("direct:sendDonationThankYou")
                    .log("Sending donation thank you email for: \${body}")
                    .to(EMAIL_NOTIFICATION_PRODUCER)

                // Email notification queue consumer
                from(EMAIL_NOTIFICATION_CONSUMER)
                    .log("Processing email notification: \${body}")
                    .to("bean:emailService?method=sendEmail")

                // Error handler route
                from("direct:errorHandler")
                    .log("Handling error: \${body}")
                    .to("bean:auditService?method=logError")

                // --- Stored procedure routing ------------------------------------------------
                // Generic procedure caller:
                // Expects header "procedureName" to be present. Request body and other headers
                // are available to the databaseService for parameter mapping.
                from("direct:callProcedure")
                    .log("Calling stored procedure: \${header.procedureName} params=\${headers}")
                    // delegate to a bean that performs the JDBC/stored-proc call.
                    // Implement databaseService.callProcedure(exchange) to inspect header/body.
                    .to("bean:databaseService?method=callProcedure")
                    .choice()
                    // Example: audit registrations
                    .`when`(header("procedureName").isEqualTo("RegisterMemberForEvent"))
                    .to("bean:auditService?method=logEventRegistration")
                    .end()
                /**
                 * Configures API routes for user management operations.
                 *
                 * This function sets up RESTful endpoints for user-related operations including:
                 * - User authentication and session management
                 * - User profile retrieval and updates
                 * - User creation and registration
                 * - User deletion and account management
                 *
                 * All routes are prefixed with the base API path and include appropriate
                 * HTTP method mappings for CRUD operations.
                 *
                 * @receiver Application The Ktor application instance to configure routes on
                 */
                from("direct:GetMemberDetails")
                    .log("GetMemberDetails invoked: \${headers}")
                    // Prefer direct SQL lookup by memberId or email
                    .choice()
                    .`when`(header("memberId").isNotNull())
                    .to("sql:SELECT * FROM members WHERE id = :#memberId?outputType=SelectList")
                    .`when`(header("email").isNotNull())
                    .to("sql:SELECT * FROM members WHERE email = :#email?outputType=SelectList")
                    .otherwise()
                    // fall back to stored-proc path
                    .setHeader("procedureName", constant("GetMemberDetails"))
                    .to("direct:callProcedure")
                    .end()

                from("direct:AddMemberToFamily")
                    .setHeader("procedureName", constant("AddMemberToFamily"))
                    .to("direct:callProcedure")

                from("direct:GetMemberDonationSummary")
                    // Returns donation summary rows for a memberId
                    .choice()
                    .`when`(header("memberId").isNotNull())
                    .to(
                        """
                        sql:SELECT member_id, 
                        SUM(amount) as total, 
                        COUNT(*) as count 
                        FROM donations 
                        WHERE member_id = :#memberId 
                        GROUP BY member_id?outputType=SelectList
                        """.trimIndent(),
                    )
                    .otherwise()
                    .setHeader("procedureName", constant("GetMemberDonationSummary"))
                    .to("direct:callProcedure")
                    .end()

                from("direct:GenerateGivingStatement")
                    .setHeader("procedureName", constant("GenerateGivingStatement"))
                    .to("direct:callProcedure")

                from("direct:GetTopDonors")
                    // Simple SQL for top donors by total amount
                    .to(
                        """
                        sql:SELECT m.id as member_id, m.first_name, m.last_name, 
                            SUM(d.amount) as total 
                        FROM donations d 
                        JOIN members m ON d.member_id = m.id 
                        GROUP BY m.id 
                        ORDER BY total DESC 
                        LIMIT 10?outputType=SelectList
                        """.trimIndent(),
                    )

                from("direct:RegisterMemberForEvent")
                    .setHeader("procedureName", constant("RegisterMemberForEvent"))
                    .to("direct:callProcedure")

                from("direct:AddMemberToGroup")
                    .setHeader("procedureName", constant("AddMemberToGroup"))
                    .to("direct:callProcedure")

                from("direct:GetGroupMembership")
                    .setHeader("procedureName", constant("GetGroupMembership"))
                    .to("direct:callProcedure")

                from("direct:GenerateMonthlyReport")
                    .setHeader("procedureName", constant("GenerateMonthlyReport"))
                    .to("direct:callProcedure")

                from("direct:SearchMembers")
                    .choice()
                    .`when`(header("q").isNotNull())
                    .setHeader("qEsc", simple("%${'$'}{header.q}%"))
                    .to(
                        """sql:SELECT * FROM members 
                            WHERE first_name LIKE :#qEsc 
                            OR last_name LIKE :#qEsc 
                            OR email LIKE :#qEsc?outputType=SelectList""",
                    )
                    .otherwise()
                    .setHeader("procedureName", constant("SearchMembers"))
                    .to("direct:callProcedure")
                    .end()

                from("direct:GetEventAttendanceStats")
                    .setHeader("procedureName", constant("GetEventAttendanceStats"))
                    .to("direct:callProcedure")

                // Route to record a tithe. Expects headers: memberId, salary (optional), paymentMethod (optional)
                from("direct:recordTithe")
                    .log("recordTithe called: headers=\${headers}")
                    .choice()
                    .`when`(header("salary").isNotNull())
                    // If salary is provided, call record_tithe directly with salary
                    .to("sql:CALL record_tithe(:#memberId, :#salary, :#paymentMethod)?outputType=SelectOne")
                    // extract donation_id from the returned row
                    .setBody(simple("${'$'}{body[donation_id]}"))
                    .`when`(header("memberId").isNotNull())
                    // If only memberId provided, try helper procedure that reads salary from members table
                    .to("sql:CALL record_tithe_from_member(:#memberId, :#paymentMethod)?outputType=SelectOne")
                    .setBody(simple("${'$'}{body[donation_id]}"))
                    .otherwise()
                    // Fallback: delegate to generic procedure caller bean
                    .setHeader("procedureName", constant("record_tithe"))
                    .to("direct:callProcedure")
                    .end()
            }
        }

    // Utility function using Arrow's Either for error handling
    fun <T> safeExecute(operation: () -> T): Either<Exception, T> =
        try {
            operation().right()
        } catch (e: Exception) {
            logger.error("Operation failed", e)
            e.left()
        }
}
