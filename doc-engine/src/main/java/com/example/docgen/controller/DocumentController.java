package com.example.docgen.controller;

import com.example.docgen.service.DocumentBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 文档生成控制器
 * 处理文档生成和下载请求
 */
@RestController
@RequestMapping("/api/doc")
@CrossOrigin(origins = "*")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentBuilderService documentBuilderService;

    public DocumentController(DocumentBuilderService documentBuilderService) {
        this.documentBuilderService = documentBuilderService;
    }

    /**
     * 生成文档
     *
     * POST /api/doc/generate
     *
     * @param request 生成请求，包含 prompt 和 templateName
     * @return 响应包含生成的文件名
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateDocument(
            @RequestBody Map<String, String> request) {
            
        Map<String, Object> response = new HashMap<>();
            
        try {
            String prompt = request.get("prompt");
            String templateName = request.get("templateName");
                
            if (prompt == null || prompt.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "提示词不能为空");
                return ResponseEntity.badRequest().body(response);
            }
                
            if (templateName == null || templateName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "模板名称不能为空");
                return ResponseEntity.badRequest().body(response);
            }
                
            // 调用服务生成文档
            String generatedFilename = documentBuilderService.generateDocument(prompt, templateName);
                
            response.put("success", true);
            response.put("filename", generatedFilename);
            response.put("message", "文档生成成功");
                
            logger.info("文档生成成功：{}", generatedFilename);
            return ResponseEntity.ok(response);
                
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            logger.warn("文档生成失败：{}", e.getMessage());
            return ResponseEntity.badRequest().body(response);
                
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "生成失败：" + e.getMessage());
            logger.error("文档生成异常", e);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 下载文档
     *
     * GET /api/doc/download/{filename}
     *
     * @param filename 文件名
     * @return 文件下载流
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String filename) {
        try {
            File file = documentBuilderService.getGeneratedFile(filename);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            // 对文件名进行 URL 编码，防止中文乱码
            String encodedFilename = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.name())
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedFilename)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            logger.error("下载文档失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
