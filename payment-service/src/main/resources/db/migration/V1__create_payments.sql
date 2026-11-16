CREATE TABLE payments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    order_id UUID NOT NULL UNIQUE,
    idempotency_key UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(24) NOT NULL,
    provider VARCHAR(24) NOT NULL,
    provider_reference VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_payment_user_idempotency UNIQUE (user_id, idempotency_key)
);
CREATE INDEX idx_payments_user_created ON payments(user_id, created_at DESC);