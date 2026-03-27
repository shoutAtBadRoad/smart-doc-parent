package com.example.docgen.service;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 样式应用服务 - 将生成的样式应用到 Word 文档
 */
@Service
public class StyleApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(StyleApplicationService.class);

    private final StyleGenerationService styleGenerationService;

    public StyleApplicationService(StyleGenerationService styleGenerationService) {
        this.styleGenerationService = styleGenerationService;
    }

    /**
     * 将生成的样式应用到文档
     * @param documentPath 文档路径
     * @param styleDescription 样式描述（自然语言）
     * @return 应用样式后的文档
     */
    public XWPFDocument applyStyleToDocument(String documentPath, String styleDescription) 
            throws IOException {
        
        logger.info("开始应用样式到文档：{}, 样式描述：{}", documentPath, styleDescription);

        // 生成样式定义
        String styleJson = styleGenerationService.generateStyleDefinition(styleDescription);
        List<Map<String, Object>> styles = styleGenerationService.parseStylesJson(styleJson);

        // 加载文档
        try (FileInputStream fis = new FileInputStream(documentPath)) {
            XWPFDocument document = new XWPFDocument(fis);

            // 应用样式
            for (Map<String, Object> style : styles) {
                applyStyle(document, style);
            }

            logger.info("样式应用完成");
            return document;
        }
    }

    /**
     * 应用单个样式到文档
     */
    @SuppressWarnings("unchecked")
    private void applyStyle(XWPFDocument document, Map<String, Object> style) {
        String type = (String) style.get("type");
        Map<String, Object> properties = (Map<String, Object>) style.get("properties");

        switch (type) {
            case "paragraph":
                applyParagraphStyle(document, properties);
                break;
            case "table":
                applyTableStyle(document, properties);
                break;
            case "run":
                applyRunStyle(document, properties);
                break;
            default:
                logger.warn("未知的样式类型：{}", type);
        }
    }

    /**
     * 应用段落样式
     */
    @SuppressWarnings("unchecked")
    private void applyParagraphStyle(XWPFDocument document, Map<String, Object> properties) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            // 设置对齐方式
            if (properties.containsKey("alignment")) {
                String alignment = (String) properties.get("alignment");
                switch (alignment) {
                    case "center":
                        paragraph.setAlignment(ParagraphAlignment.CENTER);
                        break;
                    case "right":
                        paragraph.setAlignment(ParagraphAlignment.RIGHT);
                        break;
                    case "both":
                        paragraph.setAlignment(ParagraphAlignment.BOTH);
                        break;
                    default:
                        paragraph.setAlignment(ParagraphAlignment.LEFT);
                }
            }

            // 设置 runs 的样式
            for (XWPFRun run : paragraph.getRuns()) {
                applyRunProperties(run, properties);
            }
        }
    }

    /**
     * 应用表格样式
     */
    @SuppressWarnings("unchecked")
    private void applyTableStyle(XWPFDocument document, Map<String, Object> properties) {
        for (XWPFTable table : document.getTables()) {
            // 设置背景色
            if (properties.containsKey("backgroundColor")) {
                String bgColor = (String) properties.get("backgroundColor");
                // 遍历所有单元格设置背景色
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        cell.setColor(bgColor);
                    }
                }
            }
        }
    }

    /**
     * 应用文本运行样式
     */
    @SuppressWarnings("unchecked")
    private void applyRunStyle(XWPFDocument document, Map<String, Object> properties) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                applyRunProperties(run, properties);
            }
        }
    }

    /**
     * 应用 run 属性
     */
    @SuppressWarnings("unchecked")
    private void applyRunProperties(XWPFRun run, Map<String, Object> properties) {
        // 设置字体
        if (properties.containsKey("fontFamily")) {
            run.setFontFamily((String) properties.get("fontFamily"));
        }

        // 设置字号
        if (properties.containsKey("fontSize")) {
            int fontSize = (int) Double.parseDouble(properties.get("fontSize").toString());
            run.setFontSize(fontSize * 2); // POI 使用半磅单位
        }

        // 设置加粗
        if (properties.containsKey("bold")) {
            boolean bold = (Boolean) properties.get("bold");
            run.setBold(bold);
        }

        // 设置斜体
        if (properties.containsKey("italic")) {
            boolean italic = (Boolean) properties.get("italic");
            run.setItalic(italic);
        }

        // 设置下划线
        if (properties.containsKey("underline")) {
            String underline = (String) properties.get("underline");
            if (!"none".equals(underline)) {
                run.setUnderline(UnderlinePatterns.SINGLE);
            }
        }

        // 设置颜色
        if (properties.containsKey("color")) {
            String color = (String) properties.get("color");
            // 移除#并转换为 RGB
            if (color.startsWith("#")) {
                color = color.substring(1);
            }
            int rgb = Integer.parseInt(color, 16);
            run.setColor(String.valueOf(rgb));
        }
    }
}
