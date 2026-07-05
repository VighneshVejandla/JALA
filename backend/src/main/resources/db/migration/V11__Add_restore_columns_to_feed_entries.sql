ALTER TABLE feed_entries
ADD COLUMN restored_by UUID;

ALTER TABLE feed_entries
ADD COLUMN restored_at TIMESTAMP;

ALTER TABLE feed_entries
ADD COLUMN restoration_reason VARCHAR(500);

ALTER TABLE feed_entries
ADD CONSTRAINT fk_feed_entry_restored_by
FOREIGN KEY (restored_by)
REFERENCES users(id)
ON DELETE SET NULL;