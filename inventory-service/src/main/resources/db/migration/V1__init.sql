-- inventory-service schema
CREATE TABLE inventory_items (
    product_id    BIGINT PRIMARY KEY,
    sku           VARCHAR(60) NOT NULL,
    available_qty INT NOT NULL DEFAULT 0,
    reserved_qty  INT NOT NULL DEFAULT 0,
    version       BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE reservations (
    id         BIGSERIAL PRIMARY KEY,
    order_id   VARCHAR(36) NOT NULL,
    product_id BIGINT      NOT NULL,
    quantity   INT         NOT NULL,
    status     VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_res_order ON reservations (order_id);

-- Seed stock matching the product-service seed ids (1..4).
INSERT INTO inventory_items (product_id, sku, available_qty, reserved_qty) VALUES
    (1, 'ELEC-PHONE-001', 100, 0),
    (2, 'ELEC-LAPTOP-001', 50, 0),
    (3, 'FASH-TSHIRT-001', 500, 0),
    (4, 'HOME-MIXER-001', 75, 0);
