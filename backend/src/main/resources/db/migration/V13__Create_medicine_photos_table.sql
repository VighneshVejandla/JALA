CREATE TABLE medicine_photos (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    medicine_entry_id UUID NOT NULL,

    file_name VARCHAR(255) NOT NULL,

    file_path VARCHAR(1000) NOT NULL,

    content_type VARCHAR(100),

    file_size BIGINT,

    uploaded_by UUID NOT NULL,

    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_medicine_photo_entry
        FOREIGN KEY (medicine_entry_id)
        REFERENCES medicine_entries(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_medicine_photo_uploaded_by
        FOREIGN KEY (uploaded_by)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_medicine_photo_entry
ON medicine_photos(medicine_entry_id);