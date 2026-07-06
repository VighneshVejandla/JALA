ALTER TABLE pond_cycles
ALTER COLUMN species DROP NOT NULL;

ALTER TABLE pond_cycles
ALTER COLUMN stocking_date DROP NOT NULL;

ALTER TABLE pond_cycles
ALTER COLUMN shrimp_count DROP NOT NULL;