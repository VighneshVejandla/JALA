package com.jala.backend;

import com.jala.backend.testsupport.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the full application context against real Postgres with all
 * Flyway migrations applied — catches wiring, config and schema drift.
 */
class JalaApplicationIT extends IntegrationTestBase {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("application context boots with migrated schema")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.containsBean("jwtService")).isTrue();
    }
}
