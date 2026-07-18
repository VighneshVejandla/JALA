package com.jala.backend.auth.service;

import com.jala.backend.common.exception.TooManyRequestsException;
import com.jala.backend.common.util.DateTimeUtil;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brute-force protection for login: after {@link #MAX_FAILURES}
 * consecutive failures for the same employee code, further attempts are
 * rejected with HTTP 429 for {@link #LOCKOUT} minutes.
 *
 * <p>State is kept in an in-process map, so the limit is per instance
 * (single-node). Moving the counters to a shared store (e.g. Redis) so
 * the limit holds across nodes is the multi-node follow-up.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;

    private static final Duration LOCKOUT = Duration.ofMinutes(15);

    private static final String LOCKED_MESSAGE =
            "Too many failed login attempts. Please try again later.";

    private record Attempt(int failures, long lastFailureAtMillis) {
    }

    private final ConcurrentHashMap<String, Attempt> attempts =
            new ConcurrentHashMap<>();

    /**
     * @throws TooManyRequestsException when the key is currently locked
     *         out; the message is generic on purpose.
     */
    public void checkNotLocked(String key) {

        String normalized = normalize(key);
        Attempt attempt = attempts.get(normalized);

        if (attempt == null) {
            return;
        }

        // Lazily drop this key once its lockout window has elapsed, so the
        // hot path stays O(1) instead of scanning the whole map per attempt.
        if (isStale(attempt)) {
            attempts.remove(normalized, attempt);
            return;
        }

        if (attempt.failures() >= MAX_FAILURES) {
            throw new TooManyRequestsException(LOCKED_MESSAGE);
        }
    }

    public void recordFailure(String key) {

        attempts.compute(normalize(key), (k, existing) -> {

            int failures = existing == null || isStale(existing)
                    ? 1
                    : existing.failures() + 1;

            return new Attempt(failures, nowMillis());
        });
    }

    public void reset(String key) {

        attempts.remove(normalize(key));
    }

    private boolean isStale(Attempt attempt) {

        return nowMillis() - attempt.lastFailureAtMillis()
                >= LOCKOUT.toMillis();
    }

    private long nowMillis() {

        return DateTimeUtil.clock().millis();
    }

    private String normalize(String key) {

        return key == null ? "" : key.toLowerCase(Locale.ROOT);
    }
}
