CREATE TABLE conversation_reads (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    last_read_at TIMESTAMP NOT NULL,
    UNIQUE(conversation_id, user_id)
);
