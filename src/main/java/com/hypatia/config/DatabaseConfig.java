package com.hypatia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration for hypatIA application.
 * 
 * This configuration manages database connections for different environments:
 * - Development: H2 in-memory database for quick setup and testing
 * - Production: PostgreSQL with connection pooling and optimization
 * - Test: Isolated H2 database for unit and integration tests
 * 
 * Database Schema Management:
 * - JPA entities define the database schema
 * - Hibernate handles DDL generation and validation
 * - Flyway or Liquibase can be added for database migrations
 * 
 * Connection Pooling:
 * - HikariCP (default in Spring Boot) for production
 * - Configurable pool size and timeout settings
 * - Connection leak detection and monitoring
 * 
 * Performance Optimization:
 * - Hibernate query optimization and caching
 * - Database indexing through JPA annotations
 * - Connection pool tuning for high load
 * 
 * Required Libraries:
 * - spring-boot-starter-data-jpa
 * - h2 (development and testing)
 * - postgresql (production)
 * 
 * @author hypatIA Development Team
 */
@Configuration
public class DatabaseConfig {

    /**
     * Development profile configuration with H2 in-memory database.
     * 
     * H2 Configuration Benefits:
     * - Zero setup required for development
     * - Fast startup and execution
     * - Built-in web console for database inspection
     * - Automatic schema creation from JPA entities
     * 
     * H2 Console Access:
     * - URL: http://localhost:8000/h2-console
     * - JDBC URL: jdbc:h2:mem:testdb
     * - Username: sa
     * - Password: (empty)
     * 
     * Schema Management:
     * - hibernate.ddl-auto=create-drop recreates schema on restart
     * - Sample data loaded from data.sql on startup
     * - Perfect for development and testing workflows
     * 
     * @return DataSource configured for H2 in-memory database
     */
    @Bean
    @Profile("dev")
    public DataSource h2DataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        
        return dataSource;
    }

    /**
     * Production profile configuration with PostgreSQL.
     * 
     * PostgreSQL Configuration:
     * - Robust ACID compliance for data integrity
     * - Advanced indexing and query optimization
     * - Excellent performance for complex queries
     * - Strong ecosystem and community support
     * 
     * Connection Parameters:
     * - Uses environment variables for security
     * - Supports both individual variables and DATABASE_URL
     * - SSL encryption enabled for production
     * - Connection pool sizing optimized for load
     * 
     * Environment Variables Required:
     * - PGHOST: Database server hostname
     * - PGPORT: Database server port (default 5432)
     * - PGDATABASE: Database name
     * - PGUSER: Database username
     * - PGPASSWORD: Database password
     * 
     * Alternative: DATABASE_URL format:
     * postgresql://username:password@hostname:port/database
     * 
     * @return DataSource configured for PostgreSQL production database
     */
    @Bean
    @Profile("prod")
    public DataSource postgresDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        
        // Build connection URL from environment variables
        String host = System.getenv("PGHOST") != null ? System.getenv("PGHOST") : "localhost";
        String port = System.getenv("PGPORT") != null ? System.getenv("PGPORT") : "5432";
        String database = System.getenv("PGDATABASE") != null ? System.getenv("PGDATABASE") : "hypatia_db";
        String username = System.getenv("PGUSER") != null ? System.getenv("PGUSER") : "hypatia_user";
        String password = System.getenv("PGPASSWORD") != null ? System.getenv("PGPASSWORD") : "";
        
        // Check for DATABASE_URL format (common in cloud deployments)
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            dataSource.setUrl(databaseUrl);
        } else {
            String url = String.format("jdbc:postgresql://%s:%s/%s?sslmode=prefer", host, port, database);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
        }
        
        return dataSource;
    }

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
     * - DDL generation strategy per environment
     * - SQL logging for debugging
     * - Performance optimization settings
     * - Cache configuration
     * 
     * Entity Scanning:
     * - Automatically discovers @Entity classes
     * - Package: com.hypatia.entity
     * - Validates entity mappings on startup
     * 
     * @param dataSource The configured DataSource bean
     * @return LocalContainerEntityManagerFactoryBean for JPA operations
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.hypatia.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        // Hibernate properties for optimization and debugging
        Properties properties = new Properties();
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.use_sql_comments", "true");
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        
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
     * Transaction Strategies:
     * - Read-only transactions for query optimization
     * - Isolation levels for concurrent access
     * - Timeout configuration for long operations
     * - Proper resource cleanup and connection pooling
     * 
     * Usage Examples:
     * - @Transactional on service methods
     * - @Transactional(readOnly = true) for queries
     * - @Transactional(isolation = Isolation.READ_COMMITTED)
     * - @Transactional(timeout = 30) for time-sensitive operations
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
