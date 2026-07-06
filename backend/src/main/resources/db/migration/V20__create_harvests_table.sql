CREATE TABLE harvests
(
    id UUID PRIMARY KEY,

    pond_cycle_id UUID NOT NULL
        REFERENCES pond_cycles(id),

    harvest_date DATE NOT NULL,

    harvest_quantity_kg NUMERIC(12,2) NOT NULL,

    bill_photo_path VARCHAR(1000) NOT NULL,

    buyer_name VARCHAR(150),

    selling_price_per_kg NUMERIC(12,2),

    total_amount NUMERIC(14,2),

    vehicle_number VARCHAR(30),

    remarks VARCHAR(500),

    status VARCHAR(20) NOT NULL,

    uploaded_by UUID NOT NULL
        REFERENCES users(id),

    uploaded_at TIMESTAMP NOT NULL
);