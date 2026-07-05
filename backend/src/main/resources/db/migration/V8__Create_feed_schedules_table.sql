CREATE TABLE feed_schedules (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    pond_cycle_id UUID NOT NULL,

    session_number INTEGER NOT NULL,

    feeding_time TIME NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_feed_schedule_cycle
        FOREIGN KEY (pond_cycle_id)
        REFERENCES pond_cycles(id)
        ON DELETE RESTRICT,

    CONSTRAINT uk_cycle_session
        UNIQUE (pond_cycle_id, session_number),

    CONSTRAINT uk_cycle_time
        UNIQUE (pond_cycle_id, feeding_time)
);

CREATE INDEX idx_feed_schedule_cycle
ON feed_schedules(pond_cycle_id);

CREATE INDEX idx_feed_schedule_time
ON feed_schedules(feeding_time);
