package com.churchapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.churchapp.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // JPA and transaction configuration is handled by Spring Boot auto-configuration
    // This class provides explicit configuration if needed
}
