CREATE TABLE comments
    (
        id BIGSERIAL
            PRIMARY KEY,
        video_id BIGINT NOT NULL
            REFERENCES videos (id),
        user_id VARCHAR(100) NOT NULL,
        text VARCHAR(500) NOT NULL,
        created_at TIMESTAMP DEFAULT NOW()
    );

CREATE INDEX idx_comments_video_created ON comments (video_id, created_at DESC);