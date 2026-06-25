package com.rinko.oss.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.oss.model.vo.FileVersionVO;
import com.rinko.oss.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/oss/files")
@Tag(name = "File Versioning", description = "文件版本管理接口")
public class VersionController {

    private final FileService fileService;

    public VersionController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{fileId}/versions")
    @Operation(summary = "列出文件版本")
    public ApiResponse<List<FileVersionVO>> listVersions(
            @PathVariable long fileId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(fileService.listVersions(fileId).stream()
                .map(FileVersionVO::from)
                .toList());
    }

    @PostMapping("/{fileId}/rollback/{version}")
    @Operation(summary = "回滚到指定版本")
    public ApiResponse<Void> rollback(@PathVariable long fileId, @PathVariable int version) {
        fileService.rollback(fileId, version);
        return ApiResponse.success();
    }
}
