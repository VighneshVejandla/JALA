-- Site-scoped authorization: restricted roles (SUPERVISOR, WORKER,
-- DRIVER) may only touch data belonging to sites they are assigned to.
-- ADMIN and MANAGER remain unrestricted.
CREATE TABLE user_sites (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    site_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_sites_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_sites_site
        FOREIGN KEY (site_id)
        REFERENCES sites(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_user_site UNIQUE (user_id, site_id)
);

CREATE INDEX idx_user_sites_user ON user_sites(user_id);

CREATE INDEX idx_user_sites_site ON user_sites(site_id);

-- Grandfather existing restricted users with access to every current
-- site so behaviour does not change for them on deploy; admins prune
-- assignments afterwards.
INSERT INTO user_sites (id, user_id, site_id)
SELECT gen_random_uuid(), u.id, s.id
FROM users u
JOIN roles r ON r.id = u.role_id
CROSS JOIN sites s
WHERE r.name IN ('SUPERVISOR', 'WORKER', 'DRIVER');
