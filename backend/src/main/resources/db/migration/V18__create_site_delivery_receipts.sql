CREATE TABLE site_delivery_receipts (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    site_delivery_id UUID NOT NULL,

    photo_path VARCHAR(1000) NOT NULL,

    remarks VARCHAR(500),

    uploaded_by UUID NOT NULL,

    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    cancelled_by UUID,

    cancelled_at TIMESTAMP,

    cancellation_reason VARCHAR(500),

    restored_by UUID,

    restored_at TIMESTAMP,

    restoration_reason VARCHAR(500),

    CONSTRAINT fk_receipt_site_delivery
        FOREIGN KEY (site_delivery_id)
        REFERENCES site_deliveries(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_receipt_uploaded_by
        FOREIGN KEY (uploaded_by)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_receipt_cancelled_by
        FOREIGN KEY (cancelled_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_receipt_restored_by
        FOREIGN KEY (restored_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_receipt_site_delivery
ON site_delivery_receipts(site_delivery_id);

CREATE INDEX idx_receipt_status
ON site_delivery_receipts(status);