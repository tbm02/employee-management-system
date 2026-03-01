-- Migration: user_entity_creation
-- Created at: 2026-03-01T13:13:36.210280846

BEGIN;

-- Write your SQL here
CREATE TABLE users (
                           id BIGSERIAL PRIMARY KEY,
                           emp_id VARCHAR(20) NOT NULL UNIQUE,
                           first_name VARCHAR(100) NOT NULL,
                           last_name VARCHAR(100) NOT NULL,
                           email VARCHAR(150) NOT NULL UNIQUE,
                           password VARCHAR(255) NOT NULL,
                           role VARCHAR(50) NOT NULL,
                           department_id BIGINT,
                           enabled BOOLEAN DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           created_by BIGINT,
                           modified_at TIMESTAMP,
                           modified_by BIGINT,
                           CONSTRAINT fk_department FOREIGN KEY (department_id) REFERENCES departments(id)  ON DELETE SET NULL
);

CREATE TABLE user_details (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_id BIGINT NOT NULL UNIQUE,
                                  phone_number VARCHAR(20),
                                  address VARCHAR(255),
                                  date_of_birth DATE,
                                  joining_date DATE,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  created_by BIGINT,
                                  modified_at TIMESTAMP,
                                  modified_by BIGINT,
                                  CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


COMMIT;
