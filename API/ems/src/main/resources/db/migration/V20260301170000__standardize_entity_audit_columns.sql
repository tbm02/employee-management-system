BEGIN;

-- Departments: add missing audit fields.
ALTER TABLE departments
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS modified_by BIGINT;

-- Goals: rename legacy updated_at to modified_at for consistency.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'goals'
          AND column_name = 'updated_at'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'goals'
          AND column_name = 'modified_at'
    ) THEN
        ALTER TABLE goals RENAME COLUMN updated_at TO modified_at;
    END IF;
END $$;

ALTER TABLE goals
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS modified_by BIGINT;

-- Backfill audit columns with safe defaults.
UPDATE users
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    created_by = COALESCE(created_by, 1),
    modified_at = COALESCE(modified_at, created_at, CURRENT_TIMESTAMP),
    modified_by = COALESCE(modified_by, 1);

UPDATE user_details
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    created_by = COALESCE(created_by, 1),
    modified_at = COALESCE(modified_at, created_at, CURRENT_TIMESTAMP),
    modified_by = COALESCE(modified_by, 1);

UPDATE departments
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    created_by = COALESCE(created_by, 1),
    modified_at = COALESCE(modified_at, created_at, CURRENT_TIMESTAMP),
    modified_by = COALESCE(modified_by, 1);

UPDATE goals
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    created_by = COALESCE(created_by, 1),
    modified_at = COALESCE(modified_at, created_at, CURRENT_TIMESTAMP),
    modified_by = COALESCE(modified_by, 1);

-- Enforce required audit columns.
ALTER TABLE users
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_by SET NOT NULL,
    ALTER COLUMN modified_at SET NOT NULL,
    ALTER COLUMN modified_by SET NOT NULL;

ALTER TABLE user_details
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_by SET NOT NULL,
    ALTER COLUMN modified_at SET NOT NULL,
    ALTER COLUMN modified_by SET NOT NULL;

ALTER TABLE departments
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_by SET NOT NULL,
    ALTER COLUMN modified_at SET NOT NULL,
    ALTER COLUMN modified_by SET NOT NULL;

ALTER TABLE goals
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_by SET NOT NULL,
    ALTER COLUMN modified_at SET NOT NULL,
    ALTER COLUMN modified_by SET NOT NULL;

COMMIT;
