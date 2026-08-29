-- payment-service schema
CREATE TABLE payments (
    id             VARCHAR(36) PRIMARY KEY,
    order_id       VARCHAR(36)   NOT NULL,
    user_id        BIGINT        NOT NULL,
    amount         NUMERIC(12,2) NOT NULL,
    currency       VARCHAR(3)    NOT NULL,
    status         VARCHAR(16)   NOT NULL,
    failure_reason VARCHAR(300),
    gateway_ref    VARCHAR(64),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_payments_order ON payments (order_id);
