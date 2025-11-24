CREATE TABLE schedule_state
(
    id          BIGINT PRIMARY KEY,
    month_day   VARCHAR(5) NOT NULL,
    short_state TEXT       NOT NULL,
    full_state  TEXT       NOT NULL,
    updated_at  TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
