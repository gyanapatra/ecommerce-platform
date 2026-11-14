CREATE TABLE customer_notifications (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    order_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_customer_notifications_user_created
    ON customer_notifications (user_id, created_at DESC);
