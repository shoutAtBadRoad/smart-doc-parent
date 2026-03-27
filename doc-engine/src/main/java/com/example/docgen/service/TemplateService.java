package com.example.docgen.service;

import com.example.docgen.config.FileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板服务类
 * 处理模板文件的上传和列表查询
 */
@Service
public class TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);

    private final FileConfig fileConfig;

    public TemplateService(FileConfig fileConfig) {
        this.fileConfig = fileConfig;
    }

    /**
     * 上传模板文件
     *
     * @param file 上传的文件
     * @return 保存后的文件名
     * @throws IOException 文件保存异常时抛出
     */
    public String uploadTemplate(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".docx")) {
            throw new IllegalArgumentException("只支持 .docx 格式的模板文件");
        }

        // 确保模板目录存在
        Path templateDir = Paths.get(fileConfig.getTemplatePath());
        if (!Files.exists(templateDir)) {
            Files.createDirectories(templateDir);
        }

        // 保存文件
        Path filePath = templateDir.resolve(originalFilename);
        file.transferTo(filePath.toFile());

        logger.info("模板文件已保存：{}", filePath.toAbsolutePath());
        return originalFilename;
    }

    /**
     * 获取所有模板文件列表
     *
     * @return 模板文件名列表
     */
    public List<String> listTemplates() {
        List<String> templates = new ArrayList<>();

        Path templateDir = Paths.get(fileConfig.getTemplatePath());
        if (!Files.exists(templateDir)) {
            logger.warn("模板目录不存在：{}", templateDir.toAbsolutePath());
            return templates;
        }

        try {
            Files.list(templateDir)
                .filter(path -> path.toString().toLowerCase().endsWith(".docx"))
                .forEach(path -> templates.add(path.getFileName().toString()));

            logger.info("找到 {} 个模板文件", templates.size());
        } catch (IOException e) {
            logger.error("读取模板目录失败", e);
        }

        return templates;
    }
}
