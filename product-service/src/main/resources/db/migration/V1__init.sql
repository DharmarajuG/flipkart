-- product-service schema
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(300)
);

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    sku         VARCHAR(60)   NOT NULL,
    name        VARCHAR(200)  NOT NULL,
    description VARCHAR(2000),
    price       NUMERIC(12,2) NOT NULL,
    currency    VARCHAR(3)    NOT NULL DEFAULT 'INR',
    category_id BIGINT REFERENCES categories (id),
    image_url   VARCHAR(500),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_products_sku ON products (sku);
CREATE INDEX idx_products_category ON products (category_id);

-- Seed data
INSERT INTO categories (name, description) VALUES
    ('Electronics', 'Phones, laptops and gadgets'),
    ('Fashion', 'Clothing and accessories'),
    ('Home', 'Home and kitchen');

INSERT INTO products (sku, name, description, price, currency, category_id, image_url, active) VALUES
    ('ELEC-PHONE-001', 'Krishna Phone X', '6.5-inch AMOLED, 128GB', 24999.00, 'INR', 1, 'https://cdn.krishna.shop/p/phone-x.jpg', TRUE),
    ('ELEC-LAPTOP-001', 'Krishna Ultrabook 14', 'Ryzen 7, 16GB, 512GB SSD', 64999.00, 'INR', 1, 'https://cdn.krishna.shop/p/ultrabook.jpg', TRUE),
    ('FASH-TSHIRT-001', 'Cotton Round-neck Tee', 'Premium combed cotton', 599.00, 'INR', 2, 'https://cdn.krishna.shop/p/tee.jpg', TRUE),
    ('HOME-MIXER-001', 'PowerMix 750W Mixer', '3-jar mixer grinder', 3499.00, 'INR', 3, 'https://cdn.krishna.shop/p/mixer.jpg', TRUE);
