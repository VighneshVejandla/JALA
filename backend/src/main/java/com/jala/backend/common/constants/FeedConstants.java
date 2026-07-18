package com.jala.backend.common.constants;

import java.math.BigDecimal;

public final class FeedConstants {

    private FeedConstants() {
    }

    public static final BigDecimal DEFAULT_BAG_WEIGHT_KG =
            BigDecimal.valueOf(25);

    /** Available-feed level at or below which a site counts as low. */
    public static final BigDecimal LOW_INVENTORY_THRESHOLD_KG =
            BigDecimal.valueOf(150);
}