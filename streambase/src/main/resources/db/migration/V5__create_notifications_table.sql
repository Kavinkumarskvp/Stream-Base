CREATE TABLE notifications
    (
        id BIGSERIAL
            PRIMARY KEY,
        subscriber_id VARCHAR(100) NOT NULL,
        creator_id VARCHAR(100) NOT NULL,
        video_id BIGINT NOT NULL,
        video_title VARCHAR(255) NOT NULL,
        is_read BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT NOW()
    );

CREATE INDEX idx_notifications_subscriber ON notifications (subscriber_id, created_at DESC);