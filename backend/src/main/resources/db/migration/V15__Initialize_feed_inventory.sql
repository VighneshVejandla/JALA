INSERT INTO feed_inventory (
    site_id,
    total_received_kg,
    total_consumed_kg,
    available_kg,
    updated_at
)
SELECT
    s.id,
    0,
    0,
    0,
    CURRENT_TIMESTAMP
FROM sites s
WHERE NOT EXISTS (
    SELECT 1
    FROM feed_inventory fi
    WHERE fi.site_id = s.id
);