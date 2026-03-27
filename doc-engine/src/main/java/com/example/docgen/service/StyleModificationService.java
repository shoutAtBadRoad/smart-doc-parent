package com.example.docgen.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * 样式修改服务 - 修改现有文档的样式
 */
@Service
public class StyleModificationService {

    private static final Logger logger = LoggerFactory.getLogger(StyleModificationService.class);

    private final StyleGenerationService styleGenerationService;
    private final StyleApplicationService styleApplicationService;

    public StyleModificationService(StyleGenerationService styleGenerationService, 
                                   StyleApplicationService styleApplicationService) {
        this.styleGenerationService = styleGenerationService;
        this.styleApplicationService = styleApplicationService;
    }

    /**
     * 修改现有文档的样式
     * @param documentPath 文档路径
     * @param modificationDescription 修改描述（自然语言）
     * @return 修改后的文档
     */
    public XWPFDocument modifyDocumentStyle(String documentPath, String modificationDescription) 
            throws IOException {
        
        logger.info("开始修改文档样式：{}, 修改要求：{}", documentPath, modificationDescription);

        // 1. 分析现有样式
        String styleAnalysis = analyzeExistingStyles(documentPath);
        logger.info("现有样式分析：{}", styleAnalysis);

        // 2. 生成修改方案
        String modificationPlan = generateModificationPlan(styleAnalysis, modificationDescription);
        logger.info("修改方案：{}", modificationPlan);

        // 3. 应用修改
        return styleApplicationService.applyStyleToDocument(documentPath, modificationPlan);
    }

    /**
     * 分析现有文档样式
     */
    private String analyzeExistingStyles(String documentPath) {
        try (FileInputStream fis = new FileInputStream(documentPath)) {
            XWPFDocument document = new XWPFDocument(fis);
            
            StringBuilder analysis = new StringBuilder();
            analysis.append("文档样式分析结果：\n");
            
            // 分析段落样式
            int paragraphCount = 0;
            for (XWPFParagraph para : document.getParagraphs()) {
                paragraphCount++;
                analysis.append("段落 ").append(paragraphCount).append(": ");
                
                if (para.getRuns() != null && !para.getRuns().isEmpty()) {
                    XWPFRun firstRun = para.getRuns().get(0);
                    if (firstRun.isBold()) {
                        analysis.append("加粗，");
                    }
                    if (firstRun.isItalic()) {
                        analysis.append("斜体，");
                    }
                    analysis.append("字号：").append(firstRun.getFontSize()).append(", ");
                }
                analysis.append("\n");
            }
            
            // 分析表格
            int tableCount = 0;
            for (XWPFTable table : document.getTables()) {
                tableCount++;
                analysis.append("表格 ").append(tableCount).append(": ")
                       .append(table.getRows().size()).append("行\n");
            }
            
            return analysis.toString();
        } catch (IOException e) {
            logger.error("分析文档样式失败", e);
            return "无法分析文档样式";
        }
    }

    /**
     * 生成样式修改方案
     */
    private String generateModificationPlan(String styleAnalysis, String modificationDescription) {
        // 直接返回修改描述，由 StyleApplicationService 处理
        return modificationDescription;
    }
}
