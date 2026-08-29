-- user-service schema
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    phone         VARCHAR(20),
    role          VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_users_email ON users (email);

CREATE TABLE addresses (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    line1      VARCHAR(200) NOT NULL,
    line2      VARCHAR(200),
    city       VARCHAR(80)  NOT NULL,
    state      VARCHAR(80)  NOT NULL,
    pincode    VARCHAR(12)  NOT NULL,
    country    VARCHAR(60)  NOT NULL,
    is_default BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_addresses_user ON addresses (user_id);

-- Seed a DISABLED admin placeholder. The password_hash below is NOT a real
-- credential — generate a bcrypt hash and enable this row (or register + promote
-- via SQL) before using it. Kept disabled so it can never be logged into as-is.
INSERT INTO users (email, password_hash, full_name, phone, role, enabled)
VALUES ('admin@krishna.shop',
        'REPLACE_WITH_BCRYPT_HASH',
        'Platform Admin', NULL, 'ADMIN', FALSE);
