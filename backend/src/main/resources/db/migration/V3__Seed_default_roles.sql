INSERT INTO roles (name, description)
VALUES
('ADMIN', 'System Administrator'),
('MANAGER', 'Farm Manager'),
('SUPERVISOR', 'Site Supervisor'),
('WORKER', 'Farm Worker'),
('DRIVER', 'Transport Driver')
ON CONFLICT (name) DO NOTHING;