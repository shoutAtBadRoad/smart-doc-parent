package com.example.docgen.controller;

import com.example.docgen.service.TemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板控制器
 * 处理模板上传和列表查询请求
 */
@RestController
@RequestMapping("/api/template")
@CrossOrigin(origins = "*")
public class TemplateController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * 上传模板文件
     * 
     * POST /api/template/upload
     * 
     * @param file 上传的 Word 模板文件
     * @return 响应包含文件名
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadTemplate(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String filename = templateService.uploadTemplate(file);
            response.put("success", true);
            response.put("filename", filename);
            response.put("message", "模板上传成功");
            
            logger.info("模板上传成功：{}", filename);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            logger.warn("模板上传失败：{}", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "上传失败：" + e.getMessage());
            logger.error("模板上传异常", e);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取模板列表
     * 
     * GET /api/template/list
     * 
     * @return 模板文件名列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listTemplates() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> templates = templateService.listTemplates();
            response.put("success", true);
            response.put("templates", templates);
            response.put("count", templates.size());
            
            logger.info("查询模板列表，共 {} 个", templates.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
            logger.error("查询模板列表异常", e);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
