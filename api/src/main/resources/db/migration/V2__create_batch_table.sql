CREATE TABLE batch (
    id BIGSERIAL PRIMARY KEY,
    operation VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_items INTEGER NOT NULL,
    processed_items INTEGER NOT NULL DEFAULT 0,
    success_count INTEGER NOT NULL DEFAULT 0,
    conflict_count INTEGER NOT NULL DEFAULT 0,
    failure_count INTEGER NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
);
