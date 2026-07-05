CREATE UNIQUE INDEX uk_active_cycle_per_pond
ON pond_cycles (pond_id, status)
WHERE status = 'ACTIVE';