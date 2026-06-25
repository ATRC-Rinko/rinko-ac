-- =====================================================
-- V1: 日志表（ClickHouse MergeTree）
-- =====================================================
CREATE TABLE IF NOT EXISTS logs
(
    timestamp
    DateTime64
(
    3,
    'Asia/Shanghai'
),
    level String,
    service String,
    traceId String,
    spanId String,
    class String,
    message String,
    thread String,
    context String,
    exception Nullable
(
    String
),
    exceptionClass Nullable
(
    String
)
    ) ENGINE = MergeTree
(
)
    PARTITION BY toYYYYMMDD
(
    timestamp
)
    ORDER BY
(
    timestamp,
    service
);
