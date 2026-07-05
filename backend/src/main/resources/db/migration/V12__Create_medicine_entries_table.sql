CREATE TABLE medicine_entries (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    pond_cycle_id UUID NOT NULL,

    quantity DECIMAL(10,2),

    unit VARCHAR(10),

    remarks VARCHAR(500),

    status VARCHAR(20)
        NOT NULL DEFAULT 'ACTIVE',

    created_by UUID NOT NULL,

    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    cancelled_by UUID,

    cancelled_at TIMESTAMP,

    cancellation_reason VARCHAR(500),

    restored_by UUID,

    restored_at TIMESTAMP,

    restoration_reason VARCHAR(500),

    CONSTRAINT fk_medicine_cycle
        FOREIGN KEY (pond_cycle_id)
        REFERENCES pond_cycles(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_medicine_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_medicine_cancelled_by
        FOREIGN KEY (cancelled_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_medicine_restored_by
        FOREIGN KEY (restored_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_medicine_cycle
ON medicine_entries(pond_cycle_id);

CREATE INDEX idx_medicine_status
ON medicine_entries(status);