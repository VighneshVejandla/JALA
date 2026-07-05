CREATE TABLE users (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    role_id UUID NOT NULL,

    employee_code VARCHAR(20) UNIQUE NOT NULL,

    full_name VARCHAR(150) NOT NULL,

    email VARCHAR(150),

    phone VARCHAR(20),

    password_hash VARCHAR(255) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email UNIQUE (email),

    CONSTRAINT uq_users_phone UNIQUE (phone),

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)

);