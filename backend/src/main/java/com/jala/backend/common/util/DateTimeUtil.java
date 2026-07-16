package com.jala.backend.common.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class DateTimeUtil {

    private static final ZoneId ZONE =
            ZoneId.of("Asia/Kolkata");

    private static final Clock CLOCK =
            Clock.system(ZONE);

    private DateTimeUtil() {
    }

    public static LocalDate today() {
        return LocalDate.now(CLOCK);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(CLOCK);
    }

    public static Clock clock() {
        return CLOCK;
    }
}