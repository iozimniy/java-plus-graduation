DROP TABLE IF EXISTS user_action;

CREATE TABLE IF NOT EXISTS user_action (
user_id BIGINT NOT NULL,
event_id BIGINT NOT NULL,
weight DOUBLE PRECISION NOT NULL,
updated TIMESTAMP NOT NULL,
PRIMARY KEY (user_id, event_id)
);

CREATE TABLE IF EXISTS event_similarity {
event_a BIGINT NOT NULL,
event_b BIGINT NOT NULL,
similarity DOUBLE PRECISION NOT NULL,
updated TIMESTAMP NOT NULL,
PRIMARY KEY (event_a, event_b)
};
