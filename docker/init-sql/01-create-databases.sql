-- =====================================================
-- Rinko PostgreSQL 初始化脚本
-- 为每个微服务创建独立的 database
-- =====================================================

-- 创建各服务 database
CREATE DATABASE rinko_auth OWNER postgres;
CREATE DATABASE rinko_oss OWNER postgres;
CREATE DATABASE rinko_log OWNER postgres;
CREATE DATABASE rinko_notify OWNER postgres;
CREATE DATABASE rinko_scheduler OWNER postgres;

-- 为各服务创建专用 schema 权限
\c rinko_auth
CREATE SCHEMA IF NOT EXISTS auth AUTHORIZATION postgres;

\c rinko_oss
CREATE SCHEMA IF NOT EXISTS oss AUTHORIZATION postgres;

\c rinko_log
CREATE SCHEMA IF NOT EXISTS log AUTHORIZATION postgres;

\c rinko_notify
CREATE SCHEMA IF NOT EXISTS notify AUTHORIZATION postgres;

\c rinko_scheduler
CREATE SCHEMA IF NOT EXISTS scheduler AUTHORIZATION postgres;

CREATE DATABASE oap;

GRANT ALL PRIVILEGES ON DATABASE oap TO postgres;

CREATE DATABASE logs;
CREATE DATABASE metrics;

GRANT ALL PRIVILEGES ON DATABASE logs TO postgres;
GRANT ALL PRIVILEGES ON DATABASE metrics TO postgres;