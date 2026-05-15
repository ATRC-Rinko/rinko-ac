## Decisions

1. **6 位数字验证码**: `SecureRandom` 生成 100000-999999
2. **Redis 存储**: key=`auth:verify-code:{email}`，TTL=5min。校验后删除
3. **邮件发送**: RabbitMQ → `notify.queue`，channel=EMAIL，templateCode=verification_code
4. **幂等**: 60s 内重复发送同一邮箱不重新生成（Redis key 存在则跳过）
