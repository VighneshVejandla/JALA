package com.jala.backend.testsupport;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base for full-context integration tests (*IT — run by Failsafe in CI,
 * needs Docker). Boots the application against a real Postgres with the
 * production Flyway migrations applied.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            PostgresTestContainer.INSTANCE;
}
