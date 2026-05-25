package com.rinko.notify.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rinko.infra.dto.ApiResponse;
import com.rinko.infra.exception.NotFoundException;
import com.rinko.infra.id.SnowflakeIdGenerator;
import com.rinko.notify.model.dto.CreateTemplateRequest;
import com.rinko.notify.model.dto.UpdateTemplateRequest;
import com.rinko.notify.model.vo.NotificationTemplateVO;
import com.rinko.notify.repository.NotificationTemplateMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ApiResponse<List<NotificationTemplateVO>> listTemplates() {
        var templates = templateMapper.selectList(
                new LambdaQueryWrapper<com.rinko.notify.model.entity.NotificationTemplate>()
                        .orderByAsc(com.rinko.notify.model.entity.NotificationTemplate::getCode));
        return ApiResponse.success(templates.stream()
                .map(NotificationTemplateVO::from)
                .toList());
    }

    @PostMapping
    @Operation(summary = "创建模板")
    public ApiResponse<NotificationTemplateVO> createTemplate(@RequestBody CreateTemplateRequest req, HttpServletResponse response) {
        var template = new com.rinko.notify.model.entity.NotificationTemplate();
        template.setId(idGenerator.nextId());
        template.setCode(req.code());
        template.setName(req.name());
        template.setSubject(req.subject());
        template.setBody(req.body());
        template.setChannels(req.channels() != null ? req.channels() : "IN_APP");
        templateMapper.insert(template);
        response.setStatus(201);
        return ApiResponse.success(NotificationTemplateVO.from(template));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模板")
    public ApiResponse<NotificationTemplateVO> updateTemplate(@PathVariable long id, @RequestBody UpdateTemplateRequest req) {
        var template = templateMapper.selectById(id);
        if (template == null) {
            throw new NotFoundException("Template not found");
        }
        if (req.name() != null) template.setName(req.name());
        if (req.subject() != null) template.setSubject(req.subject());
        if (req.body() != null) template.setBody(req.body());
        if (req.channels() != null) template.setChannels(req.channels());
        templateMapper.updateById(template);
        return ApiResponse.success(NotificationTemplateVO.from(template));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public ApiResponse<Void> deleteTemplate(@PathVariable long id) {
        templateMapper.deleteById(id);
        return ApiResponse.success(null);
    }
}
