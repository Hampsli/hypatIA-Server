package com.hypatia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration for hypatIA application.
 *
 * This configuration leverages Spring Boot's auto-configuration for DataSource
 * (typically HikariCP for connection pooling) and focuses on JPA/Hibernate setup.
 *
 * @author hypatIA Development Team
 */
@Configuration
public class DatabaseConfig {

    /**
     * JPA Entity Manager Factory configuration.
     *
     * This factory manages:
     * - Entity scanning and mapping
     * - Hibernate configuration and properties
     * - Database dialect detection
     * - Query optimization settings
     *
     * Hibernate Properties:
     * - DDL generation strategy per environment (configured via application.properties)
     * - SQL logging for debugging
     * - Performance optimization settings
     * - Cache configuration
     *
     * Entity Scanning:
     * - Automatically discovers @Entity classes
     * - Package: com.hypatia.entity
     * - Validates entity mappings on startup
     *
     * @param dataSource The DataSource bean (auto-configured by Spring Boot, e.g., HikariCP)
     * @return LocalContainerEntityManagerFactoryBean for JPA operations
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource); // Spring Boot will inject the auto-configured DataSource (HikariCP)
        em.setPackagesToScan("com.hypatia.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // Hibernate properties for optimization and debugging
        Properties properties = new Properties();
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.use_sql_comments", "true");
        // These batching properties are fine and optimize performance
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");

        // You might also want to set the dialect if not auto-detected correctly
        // properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        em.setJpaProperties(properties);

        return em;
    }

    /**
     * Transaction manager configuration for database operations.
     *
     * This manager provides:
     * - ACID transaction support
     * - Declarative transaction management with @Transactional
     * - Rollback on exceptions
     * - Connection and session management
     *
     * @param entityManagerFactory The JPA entity manager factory
     * @return PlatformTransactionManager for transaction management
     */
    @Bean
    public PlatformTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}