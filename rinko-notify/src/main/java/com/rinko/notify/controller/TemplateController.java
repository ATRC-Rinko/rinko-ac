package com.rinko.notify.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.notify.model.dto.CreateTemplateRequest;
import com.rinko.notify.model.dto.UpdateTemplateRequest;
import com.rinko.notify.model.vo.NotificationTemplateVO;
import com.rinko.notify.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notify/templates")
@Tag(name = "Template Management", description = "通知模板管理接口")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    @Operation(summary = "列出所有模板")
    public ApiResponse<List<NotificationTemplateVO>> listTemplates() {
        return ApiResponse.success(templateService.listAll().stream()
                .map(NotificationTemplateVO::from)
                .toList());
    }

    @PostMapping
    @Operation(summary = "创建模板")
    public ApiResponse<NotificationTemplateVO> createTemplate(@Valid @RequestBody CreateTemplateRequest req, HttpServletResponse response) {
        response.setStatus(201);
        return ApiResponse.success(NotificationTemplateVO.from(templateService.create(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模板")
    public ApiResponse<NotificationTemplateVO> updateTemplate(@PathVariable long id, @Valid @RequestBody UpdateTemplateRequest req) {
        return ApiResponse.success(NotificationTemplateVO.from(templateService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public ApiResponse<Void> deleteTemplate(@PathVariable long id) {
        templateService.delete(id);
        return ApiResponse.success(null);
    }
}
