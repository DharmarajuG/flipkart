-- order-service schema
CREATE TABLE orders (
    id             VARCHAR(36) PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    status         VARCHAR(24)   NOT NULL,
    total_amount   NUMERIC(12,2) NOT NULL,
    currency       VARCHAR(3)    NOT NULL DEFAULT 'INR',
    failure_reason VARCHAR(300),
    payment_id     VARCHAR(64),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_orders_user ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   VARCHAR(36)   NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id BIGINT        NOT NULL,
    sku        VARCHAR(60)   NOT NULL,
    name       VARCHAR(200)  NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    quantity   INT           NOT NULL
);
CREATE INDEX idx_order_items_order ON order_items (order_id);
