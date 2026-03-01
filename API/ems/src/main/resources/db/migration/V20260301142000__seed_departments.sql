-- Migration: seed_departments
-- Created at: 2026-03-01T14:20:00

BEGIN;

INSERT INTO departments (name, description)
VALUES
    ('hr', 'Human Resources'),
    ('engineering', 'Engineering'),
    ('business', 'Business')
ON CONFLICT (name) DO NOTHING;

COMMIT;
