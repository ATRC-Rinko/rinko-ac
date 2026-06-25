-- =====================================================
-- log_level_configs 基础数据
-- 建议执行: docker exec -i postgres psql -U postgres -d rinko_log < this_file.sql
-- 或在应用启动后通过 PUT /api/v1/logs/levels 接口逐条设置
-- =====================================================

-- rinko-auth: 认证服务核心包
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000001, 'rinko-auth', 'com.rinko.auth', 'INFO', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- rinko-gateway: 网关核心包
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000002, 'rinko-gateway', 'com.rinko.gateway', 'INFO', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- rinko-gateway: 网关过滤器（auth/jwt 验证相关，保持 DEBUG 排查认证问题）
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000003, 'rinko-gateway', 'com.rinko.gateway.filter', 'DEBUG', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- rinko-oss: 对象存储核心包
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000004, 'rinko-oss', 'com.rinko.oss', 'INFO', NOW(), NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- rinko-oss: 媒体处理（ffmpeg 转码日志较多，设为 WARN 降噪）
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000005, 'rinko-oss', 'com.rinko.oss.media', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- rinko-log: 日志服务核心包
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000006, 'rinko-log', 'com.rinko.log', 'INFO', NOW(), NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- rinko-log: Kafka 消费者（网络相关，保持 DEBUG 排查连接问题）
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000007, 'rinko-log', 'com.rinko.log.consumer', 'DEBUG', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- rinko-notify: 通知服务核心包
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000008, 'rinko-notify', 'com.rinko.notify', 'INFO', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- rinko-notify: 通知渠道（EMAIL/SMS/IN_APP 发送细节，INFO 级别即可）
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000009, 'rinko-notify', 'com.rinko.notify.channel', 'INFO', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- rinko-scheduler: 调度服务核心包
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000010, 'rinko-scheduler', 'com.rinko.scheduler', 'INFO', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- Spring 框架全局降噪（所有服务通用）
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000011, 'rinko-auth', 'org.springframework', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000012, 'rinko-gateway', 'org.springframework', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000013, 'rinko-oss', 'org.springframework', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000014, 'rinko-log', 'org.springframework', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000015, 'rinko-notify', 'org.springframework', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000016, 'rinko-scheduler', 'org.springframework', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

-- Apache Kafka 客户端降噪（所有服务通用）
INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000017, 'rinko-auth', 'org.apache.kafka', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000018, 'rinko-gateway', 'org.apache.kafka', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000019, 'rinko-oss', 'org.apache.kafka', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000020, 'rinko-scheduler', 'org.apache.kafka', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;

INSERT INTO log_level_configs (id, service_name, logger_name, log_level, created_at, updated_at)
VALUES (1000021, 'rinko-notify', 'org.apache.kafka', 'WARN', NOW(),
        NOW()) ON CONFLICT (service_name, logger_name) DO NOTHING;
