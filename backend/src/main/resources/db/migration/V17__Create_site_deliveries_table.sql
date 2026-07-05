CREATE TABLE site_deliveries (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    feed_delivery_id UUID NOT NULL,

    site_id UUID NOT NULL,

    number_of_bags INTEGER NOT NULL,

    bag_weight_kg DECIMAL(12,2) NOT NULL,

    total_kg DECIMAL(12,2) NOT NULL,

    remarks VARCHAR(500),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    cancelled_by UUID,

    cancelled_at TIMESTAMP,

    cancellation_reason VARCHAR(500),

    restored_by UUID,

    restored_at TIMESTAMP,

    restoration_reason VARCHAR(500),

    CONSTRAINT fk_site_delivery_feed_delivery
        FOREIGN KEY (feed_delivery_id)
        REFERENCES feed_deliveries(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_site_delivery_site
        FOREIGN KEY (site_id)
        REFERENCES sites(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_site_delivery_cancelled_by
        FOREIGN KEY (cancelled_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_site_delivery_restored_by
        FOREIGN KEY (restored_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_site_delivery_feed_delivery
ON site_deliveries(feed_delivery_id);

CREATE INDEX idx_site_delivery_site
ON site_deliveries(site_id);