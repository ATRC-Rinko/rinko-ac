package com.rinko.notify.service;

import com.rinko.infra.id.SnowflakeIdGenerator;
import com.rinko.notify.channel.InAppChannel;
import com.rinko.notify.entity.NotificationHistory;
import com.rinko.notify.entity.NotificationTemplate;
import com.rinko.notify.repository.NotificationHistoryMapper;
import com.rinko.notify.repository.NotificationTemplateMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NotifyService {

    private final NotificationHistoryMapper historyMapper;
    private final NotificationTemplateMapper templateMapper;
    private final RabbitTemplate rabbitTemplate;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public NotifyService(NotificationHistoryMapper historyMapper, NotificationTemplateMapper templateMapper,
                           RabbitTemplate rabbitTemplate) {
        this.historyMapper = historyMapper;
        this.templateMapper = templateMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    public NotificationHistory send(String channel, String templateCode, String recipient, Map<String, String> variables) {
        NotificationTemplate template = templateMapper.findByCode(templateCode);
        if (template == null) throw new IllegalArgumentException("Template not found: " + templateCode);

        String subject = replaceVars(template.getSubject(), variables);
        String content = replaceVars(template.getBody(), variables);

        NotificationHistory history = new NotificationHistory();
        history.setId(idGenerator.nextId());
        history.setChannel(channel);
        history.setTemplateCode(templateCode);
        history.setRecipient(recipient);
        history.setSubject(subject);
        history.setContent(content);
        history.setStatus("PENDING");

        rabbitTemplate.convertAndSend("notify.queue", history);
        return history;
    }

    public Map<String, Object> sendBatch(String channel, String templateCode, List<String> recipients, Map<String, String> variables) {
        NotificationTemplate template = templateMapper.findByCode(templateCode);
        if (template == null) throw new IllegalArgumentException("Template not found: " + templateCode);

        String subject = replaceVars(template.getSubject(), variables);
        String content = replaceVars(template.getBody(), variables);

        Set<String> dedup = new LinkedHashSet<>(recipients);
        List<Long> ids = new ArrayList<>();
        List<NotificationHistory> batch = new ArrayList<>();

        for (String recipient : dedup) {
            NotificationHistory history = new NotificationHistory();
            history.setId(idGenerator.nextId());
            history.setChannel(channel);
            history.setTemplateCode(templateCode);
            history.setRecipient(recipient);
            history.setSubject(subject);
            history.setContent(content);
            history.setStatus("PENDING");
            batch.add(history);
            ids.add(history.getId());
        }

        historyMapper.batchInsert(batch);
        batch.forEach(h -> rabbitTemplate.convertAndSend("notify.queue", h));

        return Map.of("count", dedup.size(), "notificationIds", ids);
    }

    public List<NotificationHistory> getInbox(String recipient, Boolean isRead) {
        return historyMapper.findByRecipientAndChannel(recipient, "IN_APP", isRead);
    }

    public long getUnreadCount(String recipient) {
        return historyMapper.countUnread(recipient);
    }

    public void markRead(long notificationId) {
        historyMapper.markRead(notificationId);
    }

    public NotificationTemplate getTemplate(String code) {
        return templateMapper.findByCode(code);
    }

    private String replaceVars(String text, Map<String, String> vars) {
        if (text == null || vars == null) return text;
        String result = text;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", e.getValue());
        }
        return result;
    }
}
