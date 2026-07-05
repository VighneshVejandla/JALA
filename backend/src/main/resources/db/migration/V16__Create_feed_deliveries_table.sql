CREATE TABLE feed_deliveries (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    delivered_by UUID NOT NULL,

    delivered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    remarks VARCHAR(500),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    cancelled_by UUID,

    cancelled_at TIMESTAMP,

    cancellation_reason VARCHAR(500),

    restored_by UUID,

    restored_at TIMESTAMP,

    restoration_reason VARCHAR(500),

    CONSTRAINT fk_feed_delivery_user
        FOREIGN KEY (delivered_by)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_feed_delivery_cancelled_by
        FOREIGN KEY (cancelled_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_feed_delivery_restored_by
        FOREIGN KEY (restored_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_feed_delivery_date
ON feed_deliveries(delivered_at);