package dev.synapse.core;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using TestContainers for PostgreSQL.
 * Extends this class to get a fully configured Spring Boot test environment
 * with a real PostgreSQL database running in Docker.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public abstract class BaseIntegrationTest {

    static boolean dockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    static PostgreSQLContainer<?> postgres;

    @BeforeAll
    static void beforeAll() {
        if (!dockerAvailable()) {
            return;
        }
        postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("synapse_test")
            .withUsername("test")
            .withPassword("test");
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (postgres == null) {
            return;
        }
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () ->
            "CHANGE_ME_IN_PRODUCTION_THIS_MUST_BE_AT_LEAST_256_BITS_LONG_FOR_HS256"
        );
        registry.add("secrets.encryption.key", () ->
            "dev_key_32_bytes_change_me_now!!"
        );

        // Disable Redis for simpler integration tests
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
    }
}
