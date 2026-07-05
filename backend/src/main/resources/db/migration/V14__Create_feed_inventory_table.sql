CREATE TABLE feed_inventory (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    site_id UUID NOT NULL UNIQUE,

    total_received_kg DECIMAL(12,2)
        NOT NULL DEFAULT 0,

    total_consumed_kg DECIMAL(12,2)
        NOT NULL DEFAULT 0,

    available_kg DECIMAL(12,2)
        NOT NULL DEFAULT 0,

    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_feed_inventory_site
        FOREIGN KEY (site_id)
        REFERENCES sites(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_feed_inventory_site
ON feed_inventory(site_id);