package com.example.docgen.controller;

import com.example.docgen.service.StyleApplicationService;
import com.example.docgen.service.StyleGenerationService;
import com.example.docgen.service.StyleModificationService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 样式管理控制器 - 提供样式生成、应用和修改的 REST API
 */
@RestController
@RequestMapping("/api/style")
@CrossOrigin(origins = "*")
public class StyleController {

    private static final Logger logger = LoggerFactory.getLogger(StyleController.class);

    private final StyleGenerationService styleGenerationService;
    private final StyleApplicationService styleApplicationService;
    private final StyleModificationService styleModificationService;

    public StyleController(StyleGenerationService styleGenerationService,
                          StyleApplicationService styleApplicationService,
                          StyleModificationService styleModificationService) {
        this.styleGenerationService = styleGenerationService;
        this.styleApplicationService = styleApplicationService;
        this.styleModificationService = styleModificationService;
    }

    /**
     * 生成样式定义
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateStyle(@RequestParam String description) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("生成样式：{}", description);
            String styleJson = styleGenerationService.generateStyleDefinition(description);
            
            response.put("success", true);
            response.put("styleDefinition", styleJson);
            response.put("message", "样式生成成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("生成样式失败", e);
            response.put("success", false);
            response.put("message", "生成失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 上传文档并应用样式
     */
    @PostMapping("/apply")
    public ResponseEntity<byte[]> applyStyle(@RequestParam("file") MultipartFile file,
                                              @RequestParam String styleDescription) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("文件不能为空".getBytes());
            }

            Path tempDir = Files.createTempDirectory("style_apply_");
            Path inputPath = tempDir.resolve(file.getOriginalFilename());
            Files.write(inputPath, file.getBytes());

            XWPFDocument document = styleApplicationService.applyStyleToDocument(
                inputPath.toString(), styleDescription);

            String outputFilename = "styled_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path outputPath = tempDir.resolve(outputFilename);
            
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                document.write(fos);
            }

            byte[] fileContent = Files.readAllBytes(outputPath);
            Files.deleteIfExists(inputPath);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + outputFilename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileContent);
        } catch (Exception e) {
            logger.error("应用样式失败", e);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("应用失败：" + e.getMessage()).getBytes());
        }
    }

    /**
     * 修改文档样式
     */
    @PostMapping("/modify")
    public ResponseEntity<byte[]> modifyStyle(@RequestParam("file") MultipartFile file,
                                               @RequestParam String modificationDescription) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("文件不能为空".getBytes());
            }

            Path tempDir = Files.createTempDirectory("style_modify_");
            Path inputPath = tempDir.resolve(file.getOriginalFilename());
            Files.write(inputPath, file.getBytes());

            XWPFDocument document = styleModificationService.modifyDocumentStyle(
                inputPath.toString(), modificationDescription);

            String outputFilename = "modified_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path outputPath = tempDir.resolve(outputFilename);
            
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                document.write(fos);
            }

            byte[] fileContent = Files.readAllBytes(outputPath);
            Files.deleteIfExists(inputPath);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + outputFilename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileContent);
        } catch (Exception e) {
            logger.error("修改样式失败", e);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("修改失败：" + e.getMessage()).getBytes());
        }
    }

    /**
     * 解析样式 JSON
     */
    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parseStyleJson(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String jsonContent = request.get("json");
            if (jsonContent == null || jsonContent.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "JSON 内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            List<Map<String, Object>> styles = styleGenerationService.parseStylesJson(jsonContent);
            
            response.put("success", true);
            response.put("styles", styles);
            response.put("count", styles.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("解析样式 JSON 失败", e);
            response.put("success", false);
            response.put("message", "解析失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
