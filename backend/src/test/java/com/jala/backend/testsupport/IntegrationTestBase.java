package com.jala.backend.testsupport;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base for full-context integration tests (*IT — run by Failsafe).
 * Boots the application against a real Postgres with the production
 * Flyway migrations applied.
 *
 * <p>{@code disabledWithoutDocker = true} means these tests are
 * <em>skipped</em> (not failed) on a machine without Docker, so
 * {@code mvn verify} stays green locally; CI (which has Docker) runs
 * them for real.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegrationTestBase {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");
}
