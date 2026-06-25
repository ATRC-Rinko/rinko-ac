package com.rinko.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rinko.infra.exception.NotFoundException;
import com.rinko.infra.id.SnowflakeIdGenerator;
import com.rinko.notify.model.dto.CreateTemplateRequest;
import com.rinko.notify.model.dto.UpdateTemplateRequest;
import com.rinko.notify.model.entity.NotificationTemplate;
import com.rinko.notify.repository.NotificationTemplateMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {

    private final NotificationTemplateMapper templateMapper;
    private final SnowflakeIdGenerator idGenerator;

    public TemplateService(NotificationTemplateMapper templateMapper, SnowflakeIdGenerator idGenerator) {
        this.templateMapper = templateMapper;
        this.idGenerator = idGenerator;
    }

    public List<NotificationTemplate> listAll() {
        return templateMapper.selectList(
                new LambdaQueryWrapper<NotificationTemplate>()
                        .orderByAsc(NotificationTemplate::getCode));
    }

    public NotificationTemplate create(CreateTemplateRequest req) {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(idGenerator.nextId());
        template.setCode(req.code());
        template.setName(req.name());
        template.setSubject(req.subject());
        template.setBody(req.body());
        template.setChannels(req.channels() != null ? req.channels() : "IN_APP");
        templateMapper.insert(template);
        return template;
    }

    public NotificationTemplate getById(long id) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new NotFoundException("Template not found");
        }
        return template;
    }

    public NotificationTemplate update(long id, UpdateTemplateRequest req) {
        NotificationTemplate template = getById(id);
        if (req.name() != null) template.setName(req.name());
        if (req.subject() != null) template.setSubject(req.subject());
        if (req.body() != null) template.setBody(req.body());
        if (req.channels() != null) template.setChannels(req.channels());
        templateMapper.updateById(template);
        return template;
    }

    public void delete(long id) {
        templateMapper.deleteById(id);
    }
}
