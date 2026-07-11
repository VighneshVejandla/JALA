CREATE TABLE notifications
(
    id UUID PRIMARY KEY,

    type VARCHAR(30) NOT NULL,

    title VARCHAR(150) NOT NULL,

    message VARCHAR(1000) NOT NULL,

    site_id UUID NOT NULL,

    pond_id UUID,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    read_at TIMESTAMP
);