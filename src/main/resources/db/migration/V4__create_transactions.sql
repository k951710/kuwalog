CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    beetle_id       BIGINT    NOT NULL REFERENCES beetles(id),
    from_user_id    BIGINT    NOT NULL REFERENCES users(id),
    to_user_id      BIGINT    NOT NULL REFERENCES users(id),
    transferred_on  DATE      NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
