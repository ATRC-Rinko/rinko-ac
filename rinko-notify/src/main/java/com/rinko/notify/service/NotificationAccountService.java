package com.rinko.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rinko.notify.model.entity.NotificationAccount;
import com.rinko.notify.repository.NotificationAccountMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class NotificationAccountService {

    private static final Logger log = LoggerFactory.getLogger(NotificationAccountService.class);
    private final NotificationAccountMapper mapper;
    private final ObjectMapper objectMapper;

    public NotificationAccountService(NotificationAccountMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询指定 provider 下已启用的账户配置，返回第一个匹配账户的 config（JSON -> Map）。
     * 若未找到任何已启用账户则返回 null。
     */
    public Map<String, Object> getEnabledConfig(String provider) {
        LambdaQueryWrapper<NotificationAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationAccount::getProvider, provider);
        wrapper.eq(NotificationAccount::getEnabled, true);

        List<NotificationAccount> accounts = mapper.selectList(wrapper);
        if (accounts.isEmpty()) {
            log.warn("No enabled notification account found for provider: {}", provider);
            return null;
        }

        NotificationAccount account = accounts.get(0);
        try {
            return objectMapper.readValue(account.getConfig(), new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Failed to parse config JSON for account {} (provider={})", account.getId(), provider, e);
            return null;
        }
    }
}
