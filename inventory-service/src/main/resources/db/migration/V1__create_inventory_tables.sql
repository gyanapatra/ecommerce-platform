CREATE TABLE inventory_stock (
    product_id UUID PRIMARY KEY,
    on_hand INTEGER NOT NULL DEFAULT 0 CHECK (on_hand >= 0),
    reserved INTEGER NOT NULL DEFAULT 0 CHECK (reserved >= 0 AND reserved <= on_hand),
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE inventory_adjustments (
    adjustment_id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    quantity_delta INTEGER NOT NULL CHECK (quantity_delta <> 0),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE inventory_reservations (
    reservation_id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES inventory_stock(product_id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'RELEASED', 'COMMITTED')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_inventory_reservations_product_status
    ON inventory_reservations (product_id, status);