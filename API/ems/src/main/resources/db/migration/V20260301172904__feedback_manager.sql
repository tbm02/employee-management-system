-- Migration: feedback_manager
-- Created at: 2026-03-01T16:29:04.949223509

BEGIN;

CREATE TABLE If not exists feedback_session
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    month      INT          NOT NULL,
    year       INT          NOT NULL,
    is_active  BOOLEAN   DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE If not exists feedback_template
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    type       VARCHAR(20)  NOT NULL, -- SELF, PEER, MANAGER
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE If not exists feedback_question
(
    id            BIGSERIAL PRIMARY KEY,
    template_id   BIGINT NOT NULL,
    question_text TEXT   NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_question_template
        FOREIGN KEY (template_id)
            REFERENCES feedback_template (id)
            ON DELETE CASCADE
);


CREATE TABLE session_users
(
    id                  BIGSERIAL PRIMARY KEY,
    session_id          BIGINT NOT NULL,
    employee_id         BIGINT NOT NULL,
    self_template_id    BIGINT NOT NULL,
    peer_template_id    BIGINT NOT NULL,
    manager_template_id BIGINT NOT NULL,

    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_session_user_session
        FOREIGN KEY (session_id)
            REFERENCES feedback_session (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_session_user_employee
        FOREIGN KEY (employee_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_self_template
        FOREIGN KEY (self_template_id)
            REFERENCES feedback_template (id),

    CONSTRAINT fk_peer_template
        FOREIGN KEY (peer_template_id)
            REFERENCES feedback_template (id),

    CONSTRAINT fk_manager_template
        FOREIGN KEY (manager_template_id)
            REFERENCES feedback_template (id)
);

CREATE TABLE If not exists session_self_review
(
    id              BIGSERIAL PRIMARY KEY,
    session_user_id BIGINT NOT NULL,
    question_id     BIGINT NOT NULL,
    score           INT CHECK (score BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_self_review_session_user
        FOREIGN KEY (session_user_id)
            REFERENCES session_users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_self_review_question
        FOREIGN KEY (question_id)
            REFERENCES feedback_question (id)
            ON DELETE CASCADE
);

CREATE TABLE if not exists session_peer_review
(
    id              BIGSERIAL PRIMARY KEY,
    session_user_id BIGINT NOT NULL,
    reviewer_id     BIGINT NOT NULL,
    question_id     BIGINT NOT NULL,
    score           INT CHECK (score BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_peer_review_session_user
        FOREIGN KEY (session_user_id)
            REFERENCES session_users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_peer_review_reviewer
        FOREIGN KEY (reviewer_id)
            REFERENCES users (id),

    CONSTRAINT fk_peer_review_question
        FOREIGN KEY (question_id)
            REFERENCES feedback_question (id)
            ON DELETE CASCADE
);


CREATE TABLE if not exists session_manager_review
(
    id              BIGSERIAL PRIMARY KEY,
    session_user_id BIGINT NOT NULL,
    reviewer_id     BIGINT NOT NULL,
    question_id     BIGINT NOT NULL,
    score           INT CHECK (score BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_manager_review_session_user
        FOREIGN KEY (session_user_id)
            REFERENCES session_users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_manager_review_reviewer
        FOREIGN KEY (reviewer_id)
            REFERENCES users (id),

    CONSTRAINT fk_manager_review_question
        FOREIGN KEY (question_id)
            REFERENCES feedback_question (id)
            ON DELETE CASCADE
);


INSERT INTO feedback_template (name, type, created_at)
VALUES ('Self Evaluation Template - Default', 'SELF', CURRENT_TIMESTAMP);

INSERT INTO feedback_question (template_id, question_text)
SELECT id, 'How do you rate your overall performance this month?'
FROM feedback_template
WHERE name = 'Self Evaluation Template - Default';

INSERT INTO feedback_question (template_id, question_text)
SELECT id, 'How effectively did you complete assigned tasks?'
FROM feedback_template
WHERE name = 'Self Evaluation Template - Default';

INSERT INTO feedback_question (template_id, question_text)
SELECT id, 'How well did you collaborate with your team?'
FROM feedback_template
WHERE name = 'Self Evaluation Template - Default';

INSERT INTO feedback_template (name, type, created_at)
VALUES ('Peer Evaluation Template - Default', 'PEER', CURRENT_TIMESTAMP);

INSERT INTO feedback_question (template_id, question_text)
SELECT id, 'How well does this employee collaborate with others?'
FROM feedback_template
WHERE name = 'Peer Evaluation Template - Default';

INSERT INTO feedback_question (template_id, question_text)
SELECT id, 'How reliable is this employee in completing tasks?'
FROM feedback_template
WHERE name = 'Peer Evaluation Template - Default';

INSERT INTO feedback_question (template_id, question_text)
SELECT id, 'How supportive is this employee toward team members?'
FROM feedback_template
WHERE name = 'Peer Evaluation Template - Default';



INSERT INTO feedback_template (name, type, created_at)
VALUES ('Manager Evaluation Template - Default', 'MANAGER', CURRENT_TIMESTAMP);

INSERT INTO feedback_question (template_id, question_text)
SELECT id, 'How would you rate the employee’s performance quality?'
FROM feedback_template
WHERE name = 'Manager Evaluation Template - Default';

INSERT INTO feedback_question (template_id, question_text)
SELECT id, 'How well does the employee meet deadlines?'
FROM feedback_template
WHERE name = 'Manager Evaluation Template - Default';

INSERT INTO feedback_question (template_id, question_text)
SELECT id, 'How proactive is the employee in solving problems?'
FROM feedback_template
WHERE name = 'Manager Evaluation Template - Default';

COMMIT;
