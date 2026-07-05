ALTER TABLE feed_entries

ADD COLUMN status VARCHAR(20)
NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE feed_entries

ADD COLUMN cancelled_by UUID;

ALTER TABLE feed_entries

ADD COLUMN cancelled_at TIMESTAMP;

ALTER TABLE feed_entries

ADD COLUMN cancellation_reason VARCHAR(500);

ALTER TABLE feed_entries

ADD CONSTRAINT fk_feed_entry_cancelled_by
FOREIGN KEY (cancelled_by)
REFERENCES users(id)
ON DELETE SET NULL;