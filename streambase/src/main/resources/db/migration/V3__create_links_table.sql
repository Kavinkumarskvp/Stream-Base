CREATE TABLE links
    (
        id BIGSERIAL
            PRIMARY KEY,
        code VARCHAR(10)
            UNIQUE,
        prefix VARCHAR(50),
        video_id BIGINT NOT NULL
            REFERENCES videos (id),
        click_count BIGINT DEFAULT 0,
        expires_at TIMESTAMP NOT NULL,
        created_at TIMESTAMP DEFAULT NOW()
    );

CREATE INDEX idx_links_code ON links(code);