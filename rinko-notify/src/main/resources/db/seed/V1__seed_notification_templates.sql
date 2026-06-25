-- =====================================================
-- notification_templates 基础数据
-- =====================================================

-- 验证码邮件（用户注册 / 登录 / 找回密码）
INSERT INTO notification_templates (id, code, name, subject, body, channels, created_at, updated_at)
VALUES (2000001, 'verification_code', '邮箱验证码', 'Rinko 邮箱验证码',
        '<html><body><h2>您的验证码</h2><p style="font-size:24px;font-weight:bold;color:#1890ff;">{code}</p><p>验证码 5 分钟内有效，请勿泄露。</p></body></html>',
        'EMAIL', NOW(), NOW()) ON CONFLICT (code) DO NOTHING;

-- 欢迎邮件（注册成功后）
INSERT INTO notification_templates (id, code, name, subject, body, channels, created_at, updated_at)
VALUES (2000002, 'welcome', '欢迎注册', '欢迎加入 Rinko！',
        '<html><body><h2>欢迎 {username}！</h2><p>您已成功注册 Rinko 账户。</p><p>如有任何问题，请联系管理员。</p></body></html>',
        'EMAIL', NOW(), NOW()) ON CONFLICT (code) DO NOTHING;

-- 站内信通知（系统通知）
INSERT INTO notification_templates (id, code, name, subject, body, channels, created_at, updated_at)
VALUES (2000003, 'system_notice', '系统通知', '系统通知', '{message}', 'IN_APP', NOW(),
        NOW()) ON CONFLICT (code) DO NOTHING;
