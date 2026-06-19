CREATE TABLE reviews (
    id              BIGSERIAL    PRIMARY KEY,
    transaction_id  BIGINT       NOT NULL REFERENCES transactions(id),
    reviewer_id     BIGINT       NOT NULL REFERENCES users(id),
    reviewee_id     BIGINT       NOT NULL REFERENCES users(id),
    rating          INT          NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review_type     VARCHAR(10)  NOT NULL CHECK (review_type IN ('NORMAL', 'FOLLOW_UP')),
    comment         TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (transaction_id, reviewer_id, review_type)
);
