CREATE TABLE pond_cycles (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    pond_id UUID NOT NULL,

    species VARCHAR(20) NOT NULL,

    stocking_date DATE NOT NULL,

    shrimp_count INTEGER NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cycle_pond
        FOREIGN KEY (pond_id)
        REFERENCES ponds(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_cycle_pond
ON pond_cycles(pond_id);

CREATE INDEX idx_cycle_status
ON pond_cycles(status);
