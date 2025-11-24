-- V1__Create_subscribers_table.sql
-- Створення таблиці для зберігання підписників Telegram бота

CREATE TABLE subscribers (
    -- ID чату Telegram як первинний ключ
                             chat_id BIGINT PRIMARY KEY,

    -- Дата і час підписки
                             subscribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Флаг активності підписки
                             active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Створення індексів для швидкого пошуку
CREATE INDEX idx_subscribers_active ON subscribers(active);
CREATE INDEX idx_subscribers_subscribed_at ON subscribers(subscribed_at);