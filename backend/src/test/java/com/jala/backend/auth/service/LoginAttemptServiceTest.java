package com.jala.backend.auth.service;

import com.jala.backend.common.exception.TooManyRequestsException;
import com.jala.backend.common.util.DateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    private static final String LOCKED_MESSAGE =
            "Too many failed login attempts. Please try again later.";

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    private void recordFailures(String key, int count) {
        for (int i = 0; i < count; i++) {
            service.recordFailure(key);
        }
    }

    @Test
    void checkNotLockedPassesWithNoFailures() {
        assertThatCode(() -> service.checkNotLocked("emp001"))
                .doesNotThrowAnyException();
    }

    @Test
    void checkNotLockedPassesUnderTheLimit() {
        recordFailures("emp001", 4);

        assertThatCode(() -> service.checkNotLocked("emp001"))
                .doesNotThrowAnyException();
    }

    @Test
    void fiveFailuresLockTheKey() {
        recordFailures("emp001", 5);

        assertThatThrownBy(() -> service.checkNotLocked("emp001"))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessage(LOCKED_MESSAGE);
    }

    @Test
    void lockoutIsScopedPerKey() {
        recordFailures("emp001", 5);

        assertThatCode(() -> service.checkNotLocked("emp002"))
                .doesNotThrowAnyException();
    }

    @Test
    void keysAreNormalizedCaseInsensitively() {
        recordFailures("EMP001", 5);

        assertThatThrownBy(() -> service.checkNotLocked("emp001"))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void nullKeysNormalizeToTheSameBucket() {
        recordFailures(null, 5);

        assertThatThrownBy(() -> service.checkNotLocked(null))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void resetClearsTheFailureCount() {
        recordFailures("emp001", 5);

        service.reset("emp001");

        assertThatCode(() -> service.checkNotLocked("emp001"))
                .doesNotThrowAnyException();
    }

    @Test
    void lockoutExpiresAfterTheLockoutWindow() {
        AtomicLong nowMillis = new AtomicLong(1_000_000_000L);

        try (MockedStatic<DateTimeUtil> mocked =
                     Mockito.mockStatic(DateTimeUtil.class)) {

            mocked.when(DateTimeUtil::clock).thenAnswer(invocation ->
                    Clock.fixed(Instant.ofEpochMilli(nowMillis.get()),
                            ZoneOffset.UTC));

            recordFailures("emp001", 5);

            assertThatThrownBy(() -> service.checkNotLocked("emp001"))
                    .isInstanceOf(TooManyRequestsException.class);

            nowMillis.addAndGet(Duration.ofMinutes(15).toMillis());

            assertThatCode(() -> service.checkNotLocked("emp001"))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void staleFailuresRestartTheCountInsteadOfAccumulating() {
        AtomicLong nowMillis = new AtomicLong(1_000_000_000L);

        try (MockedStatic<DateTimeUtil> mocked =
                     Mockito.mockStatic(DateTimeUtil.class)) {

            mocked.when(DateTimeUtil::clock).thenAnswer(invocation ->
                    Clock.fixed(Instant.ofEpochMilli(nowMillis.get()),
                            ZoneOffset.UTC));

            recordFailures("emp001", 4);

            nowMillis.addAndGet(Duration.ofMinutes(15).toMillis());

            // A stale entry restarts at 1 rather than reaching 5.
            service.recordFailure("emp001");

            assertThatCode(() -> service.checkNotLocked("emp001"))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void lockPersistsJustBeforeTheWindowElapses() {
        AtomicLong nowMillis = new AtomicLong(1_000_000_000L);

        try (MockedStatic<DateTimeUtil> mocked =
                     Mockito.mockStatic(DateTimeUtil.class)) {

            mocked.when(DateTimeUtil::clock).thenAnswer(invocation ->
                    Clock.fixed(Instant.ofEpochMilli(nowMillis.get()),
                            ZoneOffset.UTC));

            recordFailures("emp001", 5);

            nowMillis.addAndGet(Duration.ofMinutes(15).toMillis() - 1);

            assertThatThrownBy(() -> service.checkNotLocked("emp001"))
                    .isInstanceOf(TooManyRequestsException.class);
        }
    }
}
