CREATE TABLE member_profiles (
    user_id UUID PRIMARY KEY,
    member_number VARCHAR(40) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(32),
    tier VARCHAR(24) NOT NULL,
    status VARCHAR(24) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);