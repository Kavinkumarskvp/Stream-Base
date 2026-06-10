CREATE TABLE subscriptions
    (
        id BIGSERIAL
            PRIMARY KEY,
        subscriber_id VARCHAR(100) NOT NULL,
        creator_id VARCHAR(100) NOT NULL,
        created_at TIMESTAMP DEFAULT NOW(),
        UNIQUE (subscriber_id, creator_id)
    );

CREATE INDEX idx_subscriptions_creator ON subscriptions(creator_id);