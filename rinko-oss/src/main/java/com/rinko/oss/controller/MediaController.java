package com.rinko.oss.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.oss.entity.VideoResolutionEntity;
import com.rinko.oss.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/v1/oss")
@Tag(name = "Media Processing", description = "媒体处理接口")
public class MediaController {

    private final FileService fileService;

    public MediaController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/thumbnail/{fileId}")
    @Operation(summary = "获取缩略图")
    public ResponseEntity<Resource> thumbnail(@PathVariable long fileId) {
        var meta = fileService.getMetadata(fileId);
        String thumbKey = meta.getStoragePath().replace("/" + meta.getOriginalName(), "/thumb.jpg");
        try {
            InputStream stream = fileService.downloadByKey(thumbKey);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/video/{fileId}/resolutions")
    @Operation(summary = "查询视频分辨率转码状态")
    public ApiResponse<List<VideoResolutionEntity>> listResolutions(@PathVariable long fileId) {
        return ApiResponse.success(fileService.listVideoResolutions(fileId));
    }

    @GetMapping("/video/{fileId}/stream/{resolution}")
    @Operation(summary = "流式播放指定分辨率视频")
    public ResponseEntity<Resource> streamResolution(@PathVariable long fileId, @PathVariable String resolution) {
        var meta = fileService.getMetadata(fileId);
        String baseKey = meta.getStoragePath().replace("/" + meta.getOriginalName(), "");
        String resKey = baseKey + "/" + resolution + ".mp4";
        InputStream stream = fileService.downloadByKey(resKey);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/mp4"))
                .body(new InputStreamResource(stream));
    }
}
