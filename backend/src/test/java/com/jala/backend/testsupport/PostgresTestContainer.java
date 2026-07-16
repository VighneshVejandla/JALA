package com.jala.backend.testsupport;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton Postgres container shared by every integration test class,
 * so the schema is migrated once per JVM instead of once per class.
 * Ryuk reaps the container when the test JVM exits.
 */
public final class PostgresTestContainer {

    public static final PostgreSQLContainer<?> INSTANCE =
            new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        INSTANCE.start();
    }

    private PostgresTestContainer() {
    }
}
