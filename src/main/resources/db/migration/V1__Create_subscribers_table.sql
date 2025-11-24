CREATE TABLE subscribers
(
    chat_id       BIGINT PRIMARY KEY,
    subscribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active        BOOLEAN   NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_subscribers_active ON subscribers (active);
CREATE INDEX idx_subscribers_subscribed_at ON subscribers (subscribed_at);