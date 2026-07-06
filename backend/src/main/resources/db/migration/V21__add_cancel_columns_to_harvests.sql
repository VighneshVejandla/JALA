ALTER TABLE harvests
ADD COLUMN cancelled_by UUID REFERENCES users(id);

ALTER TABLE harvests
ADD COLUMN cancelled_at TIMESTAMP;

ALTER TABLE harvests
ADD COLUMN cancellation_reason VARCHAR(500);