package com.rinko.oss.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.infra.dto.PageResponse;
import com.rinko.oss.model.dto.CompleteMultipartUploadRequest;
import com.rinko.oss.model.dto.CreateDirectoryRequest;
import com.rinko.oss.model.dto.InitMultipartUploadRequest;
import com.rinko.oss.model.vo.MultipartInitVO;
import com.rinko.oss.model.vo.PartUploadVO;
import com.rinko.oss.model.vo.PresignUrlVO;
import com.rinko.oss.model.entity.FileMetadata;
import com.rinko.oss.model.vo.FileMetadataVO;
import com.rinko.oss.service.FileService;
import com.rinko.oss.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/v1/oss")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "文件管理接口")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public ApiResponse<FileMetadataVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", required = false) Long parentId,
            HttpServletResponse response) throws IOException {
        var meta = fileService.upload(file.getInputStream(),
                file.getOriginalFilename(), file.getContentType(), parentId);
        response.setStatus(201);
        return ApiResponse.success(FileMetadataVO.from(meta));
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
    public ApiResponse<PresignUrlVO> presignUrl(
            @PathVariable long fileId,
            @RequestParam(defaultValue = "600") int expires) {
        return ApiResponse.success(new PresignUrlVO(fileService.presignUrl(fileId, expires)));
    }

    @GetMapping("/files")
    @Operation(summary = "分页列出文件")
    public ApiResponse<PageResponse<FileMetadataVO>> listFiles(
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageResult = fileService.listFiles(parentId, page, size);
        var voContent = pageResult.content().stream()
                .map(FileMetadataVO::from)
                .toList();
        return ApiResponse.success(new PageResponse<>(voContent, pageResult.totalElements(), pageResult.page(), pageResult.size()));
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
    public ApiResponse<FileMetadataVO> createDirectory(@RequestBody CreateDirectoryRequest req, HttpServletResponse response) {
        response.setStatus(201);
        return ApiResponse.success(FileMetadataVO.from(fileService.createDirectory(req.name(), req.parentId())));
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
    public ApiResponse<MultipartInitVO> initMultipartUpload(@RequestBody InitMultipartUploadRequest req) {
        FileService.MultipartSession session = fileService.initiateMultipartUpload(
                req.originalName(),
                req.contentType() != null ? req.contentType() : "application/octet-stream",
                req.parentId());
        return ApiResponse.success(new MultipartInitVO(session.uploadId(), String.valueOf(session.fileId())));
    }

    @PostMapping("/upload/multipart/part")
    @Operation(summary = "上传分片")
    public ApiResponse<PartUploadVO> uploadPart(
            @RequestParam String uploadId,
            @RequestParam int partNumber,
            @RequestParam("part") MultipartFile part) throws IOException {
        StorageService.PartETag etag = fileService.uploadPart(uploadId, partNumber,
                part.getInputStream(), part.getSize());
        return ApiResponse.success(new PartUploadVO(String.valueOf(etag.partNumber()), etag.etag()));
    }

    @PostMapping("/upload/multipart/complete")
    @Operation(summary = "完成分片上传")
    public ApiResponse<FileMetadataVO> completeMultipartUpload(@RequestBody CompleteMultipartUploadRequest req, HttpServletResponse response) {
        List<StorageService.PartETag> parts = req.parts().stream()
                .map(p -> new StorageService.PartETag(p.partNumber(), p.etag()))
                .toList();
        var meta = fileService.completeMultipartUpload(req.uploadId(), parts);
        response.setStatus(201);
        return ApiResponse.success(FileMetadataVO.from(meta));
    }

    @DeleteMapping("/upload/multipart/{uploadId}")
    @Operation(summary = "取消分片上传")
    public ApiResponse<Void> abortMultipartUpload(@PathVariable String uploadId, HttpServletResponse response) {
        fileService.abortMultipartUpload(uploadId);
        response.setStatus(204);
        return ApiResponse.success();
    }
}
