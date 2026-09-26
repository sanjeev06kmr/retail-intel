CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,

    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),

    price NUMERIC(12, 2) NOT NULL,

    status VARCHAR(30) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL,

    CONSTRAINT uk_product_sku UNIQUE (sku)
);