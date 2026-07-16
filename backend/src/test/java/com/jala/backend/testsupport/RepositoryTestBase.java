package com.jala.backend.testsupport;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base for repository slice tests (*IT — run by Failsafe in CI, needs
 * Docker). Runs against real Postgres + the production Flyway schema,
 * validating custom JPQL and entity mappings for real.
 */
@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class RepositoryTestBase {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            PostgresTestContainer.INSTANCE;
}
