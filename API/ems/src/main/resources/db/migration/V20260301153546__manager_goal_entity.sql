-- Migration: manager_goal_entity
-- Created at: 2026-03-01T15:35:46.472875145

BEGIN;

-- Write your SQL here
ALTER TABLE users
ADD COLUMN manager_id BIGINT;

ALTER TABLE users
ADD CONSTRAINT fk_users_manager
FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL;


-- =========================================
-- Create goals table
-- =========================================

CREATE TABLE goals (
                       id BIGSERIAL PRIMARY KEY,
                       user_id BIGINT NOT NULL,
                       description TEXT NOT NULL,
                       quarter VARCHAR(2) NOT NULL,
                       year INT NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       approved_by BIGINT,
                       is_completed BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_goals_employee
                           FOREIGN KEY (user_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE,
                       CONSTRAINT fk_goals_approved_by
                           FOREIGN KEY (approved_by)
                            REFERENCES users(id)
                            ON DELETE SET NULL
);



COMMIT;
