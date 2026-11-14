CREATE TABLE customer_orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    idempotency_key UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL CHECK (total_amount >= 0),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_customer_orders_user_idempotency UNIQUE (user_id, idempotency_key)
);

CREATE INDEX idx_customer_orders_user_created ON customer_orders (user_id, created_at DESC);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES customer_orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 2) NOT NULL CHECK (unit_price >= 0),
    line_total NUMERIC(19, 2) NOT NULL CHECK (line_total >= 0),
    reservation_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_order_items_order_product UNIQUE (order_id, product_id)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);