## Table Design

```sql
notification_accounts (
    id BIGINT PRIMARY KEY,
    provider VARCHAR(32) NOT NULL,     -- SMTP / SENDGRID / ALIYUN_SMS / TENCENT_SMS
    name VARCHAR(64) NOT NULL,          -- 账户名称（如 "企业邮箱"）
    config JSONB NOT NULL,              -- provider-specific config as JSON
    enabled BOOLEAN DEFAULT TRUE,
    created_at / updated_at TIMESTAMP
)
```

## Config JSON Schema

**SMTP:** `{"host":"smtp.example.com","port":587,"username":"x","password":"x","from":"noreply@x.com"}`
**SendGrid:** `{"apiKey":"x","from":"noreply@x.com"}`
**AliyunSMS:** `{"accessKeyId":"x","accessKeySecret":"x","signName":"Rinko","templateCode":"SMS_xxx"}`
**TencentSMS:** `{"appId":"x","appKey":"x","signName":"Rinko","templateId":"12345"}`

## Provider Selection

`NotificationAccountService.getEnabledAccount(provider)` → 返回第一个 enabled 的账户 config。Provider 启动时加载，缓存不刷新（重启生效）。
