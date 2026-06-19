CREATE TABLE beetle_images (
    id          BIGSERIAL    PRIMARY KEY,
    beetle_id   BIGINT       NOT NULL REFERENCES beetles(id),
    image_url   VARCHAR(500) NOT NULL,
    is_primary  BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
