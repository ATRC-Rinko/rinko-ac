package com.rinko.notify.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.infra.id.SnowflakeIdGenerator;
import com.rinko.notify.entity.NotificationTemplate;
import com.rinko.notify.repository.NotificationTemplateMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notify/templates")
@Tag(name = "Template Management", description = "通知模板管理接口")
public class TemplateController {

    private final NotificationTemplateMapper templateMapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public TemplateController(NotificationTemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    @GetMapping
    @Operation(summary = "列出所有模板")
    public ApiResponse<List<NotificationTemplate>> listTemplates() {
        return ApiResponse.success(templateMapper.findAll());
    }

    @PostMapping
    @Operation(summary = "创建模板")
    public ApiResponse<NotificationTemplate> createTemplate(@RequestBody Map<String, String> body, HttpServletResponse response) {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(idGenerator.nextId());
        template.setCode(body.get("code"));
        template.setName(body.get("name"));
        template.setSubject(body.get("subject"));
        template.setBody(body.get("body"));
        template.setChannels(body.getOrDefault("channels", "IN_APP"));
        templateMapper.insert(template);
        response.setStatus(201);
        return ApiResponse.success(template);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模板")
    public ApiResponse<NotificationTemplate> updateTemplate(@PathVariable long id, @RequestBody Map<String, String> body) {
        NotificationTemplate template = templateMapper.findById(id);
        if (template == null) return ApiResponse.error(404, "Template not found");
        template.setName(body.getOrDefault("name", template.getName()));
        template.setSubject(body.getOrDefault("subject", template.getSubject()));
        template.setBody(body.getOrDefault("body", template.getBody()));
        template.setChannels(body.getOrDefault("channels", template.getChannels()));
        templateMapper.update(template);
        return ApiResponse.success(template);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public ApiResponse<Void> deleteTemplate(@PathVariable long id) {
        templateMapper.deleteById(id);
        return ApiResponse.success(null);
    }
}
