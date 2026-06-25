-- =====================================================
-- notification_accounts 基础数据
-- =====================================================

INSERT INTO notification_accounts (id, provider, name, config, enabled, created_at, updated_at)
VALUES (3000001, 'SMTP', '默认邮箱',
        '{"host":"smtp.example.com","port":587,"username":"","password":"","from":"noreply@rinko.local"}', TRUE, NOW(),
        NOW()) ON CONFLICT (provider, name) DO NOTHING;
