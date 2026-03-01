-- Migration: department_entity_creation
-- Created at: 2026-03-01T13:14:14.189117047

BEGIN;

-- Write your SQL here

CREATE TABLE departments (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(255),
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


COMMIT;
