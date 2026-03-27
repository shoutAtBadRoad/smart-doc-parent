package com.example.docgen.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文件配置类
 * 读取配置文件中的路径，并在应用启动时自动创建目录
 */
@Component
public class FileConfig {

    private static final Logger logger = LoggerFactory.getLogger(FileConfig.class);

    @Value("${doc.template-path}")
    private String templatePath;

    @Value("${doc.output-path}")
    private String outputPath;
    
    private boolean isClasspathTemplate = false;

    /**
     * 在应用启动时自动创建模板和输出目录
     */
    @PostConstruct
    public void init() {
        logger.info("初始化文档生成目录...");
        
        // 检查是否使用 classpath 方式
        if (templatePath.startsWith("classpath:")) {
            isClasspathTemplate = true;
            logger.info("使用 classpath 方式加载模板：{}", templatePath);
        } else {
            // 创建模板目录
            createDirectory(templatePath, "模板");
        }
        
        // 创建输出目录
        createDirectory(outputPath, "输出");
        
        logger.info("文档生成目录初始化完成");
    }

    /**
     * 创建目录（如果不存在）
     * 
     * @param path 目录路径
     * @param dirType 目录类型描述
     */
    private void createDirectory(String path, String dirType) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                logger.info("{}目录已创建：{}", dirType, dir.getAbsolutePath());
            } else {
                logger.error("创建{}目录失败：{}", dirType, dir.getAbsolutePath());
            }
        } else {
            logger.info("{}目录已存在：{}", dirType, dir.getAbsolutePath());
        }
    }

    /**
     * 获取模板路径
     * 
     * @return 模板路径
     */
    public String getTemplatePath() {
        return templatePath;
    }
    
    /**
     * 判断是否使用 classpath 方式加载模板
     * 
     * @return 如果是 classpath 方式返回 true
     */
    public boolean isClasspathTemplate() {
        return isClasspathTemplate;
    }

    /**
     * 获取输出路径
     * 
     * @return 输出路径
     */
    public String getOutputPath() {
        return outputPath;
    }
}
