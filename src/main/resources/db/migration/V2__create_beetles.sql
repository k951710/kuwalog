CREATE TABLE beetles (
    id              BIGSERIAL    PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id),
    father_id       BIGINT       REFERENCES beetles(id),
    mother_id       BIGINT       REFERENCES beetles(id),
    name            VARCHAR(100) NOT NULL,
    sex             VARCHAR(10)  NOT NULL CHECK (sex IN ('オス', 'メス', '不明')),
    generation      VARCHAR(20),
    locality        VARCHAR(100),
    emergence_date  VARCHAR(7),
    stage           VARCHAR(10)  NOT NULL CHECK (stage IN ('卵', '幼虫', '蛹', '成虫')),
    description     TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
