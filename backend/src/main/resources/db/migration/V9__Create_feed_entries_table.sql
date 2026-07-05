CREATE TABLE feed_entries (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    pond_cycle_id UUID NOT NULL,

    feed_schedule_id UUID NOT NULL,

    feed_date DATE NOT NULL,

    feed_size VARCHAR(10) NOT NULL,

    feed_quantity_kg DECIMAL(10,2) NOT NULL,

    remarks VARCHAR(500),

    created_by UUID,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_feed_entry_cycle
        FOREIGN KEY (pond_cycle_id)
        REFERENCES pond_cycles(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_feed_entry_schedule
        FOREIGN KEY (feed_schedule_id)
        REFERENCES feed_schedules(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_feed_entry_user
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT uk_feed_per_session_per_day
        UNIQUE (
            pond_cycle_id,
            feed_schedule_id,
            feed_date
        )
);

CREATE INDEX idx_feed_entry_cycle
ON feed_entries (pond_cycle_id);

CREATE INDEX idx_feed_entry_schedule
ON feed_entries (feed_schedule_id);

CREATE INDEX idx_feed_entry_date
ON feed_entries (feed_date);

CREATE INDEX idx_feed_entry_created_by
ON feed_entries (created_by);