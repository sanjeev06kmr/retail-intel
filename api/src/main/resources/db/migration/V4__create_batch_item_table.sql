CREATE TABLE batch_item (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    item_number INTEGER NOT NULL,

    operation VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,

    product_id BIGINT NULL,
    sku VARCHAR(100) NULL,
    name VARCHAR(255) NULL,
    description TEXT NULL,
    category VARCHAR(100) NULL,
    price NUMERIC(12, 2) NULL,
    product_status VARCHAR(30) NULL,

    expected_version BIGINT NULL,

    error_code VARCHAR(50) NULL,
    error_message VARCHAR(500) NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_batch_item_batch FOREIGN KEY (batch_id) REFERENCES batch(id),
    CONSTRAINT uq_batch_item_batch_item_number UNIQUE (batch_id, item_number)
);

CREATE INDEX idx_batch_item_batch_id ON batch_item (batch_id);
