CREATE TABLE ponds (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    site_id UUID NOT NULL,

    pond_code VARCHAR(20) NOT NULL,

    pond_name VARCHAR(100) NOT NULL,

    pond_acres DECIMAL(10,2) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pond_site
        FOREIGN KEY (site_id)
        REFERENCES sites(id)
        ON DELETE RESTRICT,

    CONSTRAINT uk_site_pond_code
        UNIQUE(site_id, pond_code)

);

CREATE INDEX idx_pond_site
ON ponds(site_id);

CREATE INDEX idx_pond_name
ON ponds(pond_name);