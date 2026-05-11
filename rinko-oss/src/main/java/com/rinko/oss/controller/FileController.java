package com.rinko.oss.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.infra.dto.PageResponse;
import com.rinko.oss.entity.FileMetadata;
import com.rinko.oss.service.FileService;
import com.rinko.oss.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/oss")
@Tag(name = "File Management", description = "文件管理接口")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public ApiResponse<FileMetadata> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", required = false) Long parentId,
            HttpServletResponse response) throws IOException {
        FileMetadata meta = fileService.upload(file.getInputStream(),
                file.getOriginalFilename(), file.getContentType(), parentId);
        response.setStatus(201);
        return ApiResponse.success(meta);
    }

    @GetMapping("/download/{fileId}")
    @Operation(summary = "下载文件")
    public ResponseEntity<Resource> download(@PathVariable long fileId) {
        FileMetadata meta = fileService.getMetadata(fileId);
        InputStream stream = fileService.download(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getContentType() != null ? meta.getContentType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getOriginalName() + "\"")
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/presign/{fileId}")
    @Operation(summary = "生成预签名下载URL")
    public ApiResponse<Map<String, String>> presignUrl(
            @PathVariable long fileId,
            @RequestParam(defaultValue = "600") int expires) {
        String url = fileService.presignUrl(fileId, expires);
        return ApiResponse.success(Map.of("url", url));
    }

    @GetMapping("/files")
    @Operation(summary = "分页列出文件")
    public ApiResponse<PageResponse<FileMetadata>> listFiles(
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(fileService.listFiles(parentId, page, size));
    }

    @DeleteMapping("/files/{fileId}")
    @Operation(summary = "删除文件")
    public ApiResponse<Void> delete(@PathVariable long fileId, HttpServletResponse response) {
        fileService.delete(fileId);
        response.setStatus(204);
        return ApiResponse.success(null);
    }

    @PostMapping("/directories")
    @Operation(summary = "创建目录")
    public ApiResponse<FileMetadata> createDirectory(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String name = body.get("name");
        Long parentId = body.containsKey("parentId") ? Long.parseLong(body.get("parentId")) : null;
        response.setStatus(201);
        return ApiResponse.success(fileService.createDirectory(name, parentId));
    }

    @GetMapping("/download/by-key")
    @Operation(summary = "通过存储路径下载")
    public ResponseEntity<Resource> downloadByKey(@RequestParam String key) {
        FileMetadata meta = fileService.getMetadata(Long.parseLong(key.split("/")[0]));
        InputStream stream = fileService.downloadByKey(key);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getContentType() != null ? meta.getContentType() : "application/octet-stream"))
                .body(new InputStreamResource(stream));
    }

    @PostMapping("/upload/multipart/init")
    @Operation(summary = "初始化分片上传")
    public ApiResponse<Map<String, String>> initMultipartUpload(@RequestBody Map<String, String> body) {
        String originalName = body.get("originalName");
        String contentType = body.getOrDefault("contentType", "application/octet-stream");
        Long parentId = body.containsKey("parentId") ? Long.parseLong(body.get("parentId")) : null;
        FileService.MultipartSession session = fileService.initiateMultipartUpload(originalName, contentType, parentId);
        return ApiResponse.success(Map.of("uploadId", session.uploadId(), "fileId", String.valueOf(session.fileId())));
    }

    @PostMapping("/upload/multipart/part")
    @Operation(summary = "上传分片")
    public ApiResponse<Map<String, String>> uploadPart(
            @RequestParam String uploadId,
            @RequestParam int partNumber,
            @RequestParam("part") MultipartFile part) throws IOException {
        StorageService.PartETag etag = fileService.uploadPart(uploadId, partNumber,
                part.getInputStream(), part.getSize());
        return ApiResponse.success(Map.of("partNumber", String.valueOf(etag.partNumber()), "etag", etag.etag()));
    }

    @PostMapping("/upload/multipart/complete")
    @Operation(summary = "完成分片上传")
    public ApiResponse<FileMetadata> completeMultipartUpload(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        String uploadId = (String) body.get("uploadId");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> partsList = (List<Map<String, Object>>) body.get("parts");
        List<StorageService.PartETag> parts = partsList.stream()
                .map(p -> new StorageService.PartETag(
                        ((Number) p.get("partNumber")).intValue(),
                        (String) p.get("etag")))
                .toList();
        FileMetadata meta = fileService.completeMultipartUpload(uploadId, parts);
        response.setStatus(201);
        return ApiResponse.success(meta);
    }

    @DeleteMapping("/upload/multipart/{uploadId}")
    @Operation(summary = "取消分片上传")
    public ApiResponse<Void> abortMultipartUpload(@PathVariable String uploadId, HttpServletResponse response) {
        fileService.abortMultipartUpload(uploadId);
        response.setStatus(204);
        return ApiResponse.success(null);
    }
}
