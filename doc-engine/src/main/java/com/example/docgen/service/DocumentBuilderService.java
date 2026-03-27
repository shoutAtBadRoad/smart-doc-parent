package com.example.docgen.service;

import com.example.docgen.config.FileConfig;
import com.example.docgen.model.DocumentBlock;
import com.example.llm.client.service.LlmService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 文档构建服务类 - 使用 Apache POI 解析 JSON 并应用 Word 样式
 */
@Service
public class DocumentBuilderService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentBuilderService.class);

    private final LlmService llmService;
    private final FileConfig fileConfig;
    private final ObjectMapper objectMapper;

    public DocumentBuilderService(LlmService llmService, FileConfig fileConfig) {
        this.llmService = llmService;
        this.fileConfig = fileConfig;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成文档 - 使用 Apache POI 解析 JSON 并应用 Word 样式
     *
     * @param prompt 提示词
     * @param templateName 模板文件名
     * @return 生成的文件名
     * @throws Exception 处理异常时抛出
     */
    public String generateDocument(String prompt, String templateName) throws Exception {
        logger.info("开始生成文档，模板：{}, 提示词：{}", templateName, prompt);

        // 1. 调用 LLM 服务获取 JSON 内容
        String jsonContent = llmService.generateContent(prompt);
        logger.debug("LLM 返回的 JSON 内容：{}", jsonContent);

        // 2. 解析 JSON 为 DocumentBlock 列表
        JsonNode rootNode = objectMapper.readTree(jsonContent);
        String title = rootNode.has("title") ? rootNode.get("title").asText() : "未命名文档";
        List<DocumentBlock> blocks = objectMapper.readValue(
                rootNode.get("blocks").toString(),
                new TypeReference<List<DocumentBlock>>() {}
        );

        logger.info("解析出 {} 个文档块", blocks.size());

        // 3. 加载模板并创建新文档
        XWPFDocument document = null;
        String generatedFilename = null;
        try {
            // 从文件系统加载模板
            Path templatePath = Paths.get(fileConfig.getTemplatePath(), templateName);
            if (!Files.exists(templatePath)) {
                throw new IllegalArgumentException("模板文件不存在：" + templateName);
            }
            
            try (FileInputStream fis = new FileInputStream(templatePath.toFile())) {
                document = new XWPFDocument(fis);
            }

            // 4. 遍历处理文档块，根据 JSON type 应用对应的 Word 样式
            processDocumentBlocks(document, blocks);

            // 5. 生成文件名并保存
            generatedFilename = generateFilename(title);
            Path outputPath = Paths.get(fileConfig.getOutputPath(), generatedFilename);

            // 确保输出目录存在
            if (!Files.exists(outputPath.getParent())) {
                Files.createDirectories(outputPath.getParent());
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                document.write(fos);
                fos.flush();
            }

            logger.info("文档生成成功：{}", generatedFilename);
            return generatedFilename;

        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * 处理文档块，将其添加到 Word 文档中
     * 核心逻辑：根据 DocumentBlock 的 type 字段自动关联到 Word 中的样式
     *
     * @param document Word 文档对象
     * @param blocks 文档块列表
     */
    private void processDocumentBlocks(XWPFDocument document, List<DocumentBlock> blocks) {
        for (DocumentBlock block : blocks) {
            String type = block.getType();

            switch (type) {
                case "H1":
                    createHeadingWithStyle(document, block.getContent(), "Heading1");
                    break;
                case "H2":
                    createHeadingWithStyle(document, block.getContent(), "Heading2");
                    break;
                case "TEXT":
                    createParagraphWithStyle(document, block.getContent(), "a");
                    break;
                case "TABLE":
                    createTable(document, block.getHeaders(), block.getData());
                    break;
                default:
                    logger.warn("未知的文档块类型：{}", type);
            }
        }
    }

    /**
     * 创建带样式的标题段落
     * @param document Word 文档
     * @param text 文本内容
     * @param styleId Word 样式 ID（如 Heading1, Heading2）
     */
    private void createHeadingWithStyle(XWPFDocument document, String text, String styleId) {
        XWPFParagraph paragraph = document.createParagraph();
        
        // 尝试设置样式 ID
        try {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP ctp = paragraph.getCTP();
            if (ctp.isSetPPr()) {
                ctp.getPPr().setPStyle(ctp.getPPr().addNewPStyle());
                ctp.getPPr().getPStyle().setVal(styleId);
            } else {
                ctp.addNewPPr().addNewPStyle().setVal(styleId);
            }
        } catch (Exception e) {
            logger.warn("设置样式 {} 失败，使用默认格式", styleId, e);
        }
        
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        
        // 如果样式设置失败，使用默认格式
        if ("Heading1".equals(styleId)) {
            run.setBold(true);
            run.setFontSize(24);
        } else if ("Heading2".equals(styleId)) {
            run.setBold(true);
            run.setFontSize(18);
        }
    }

    /**
     * 创建带样式的正文段落
     * @param document Word 文档
     * @param text 文本内容
     * @param styleId Word 样式 ID（如 a, Normal 等）
     */
    private void createParagraphWithStyle(XWPFDocument document, String text, String styleId) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.BOTH);
        paragraph.setSpacingAfter(200);
        
        // 尝试设置样式 ID
        try {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP ctp = paragraph.getCTP();
            if (ctp.isSetPPr()) {
                ctp.getPPr().setPStyle(ctp.getPPr().addNewPStyle());
                ctp.getPPr().getPStyle().setVal(styleId);
            } else {
                ctp.addNewPPr().addNewPStyle().setVal(styleId);
            }
        } catch (Exception e) {
            logger.warn("设置样式 {} 失败，使用默认格式", styleId, e);
        }
        
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(12);
    }

    /**
     * 创建表格
     */
    private void createTable(XWPFDocument document, String[] headers, String[][] data) {
        if (headers == null || headers.length == 0) {
            logger.warn("表格头部为空，跳过");
            return;
        }

        if (data == null) {
            data = new String[0][0];
        }

        int rows = data.length;
        int cols = headers.length;

        // 创建表格（包含表头共 rows+1 行）
        XWPFTable table = document.createTable(rows + 1, cols);

        // 设置表格宽度 100%
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth tblWidth = 
            table.getCTTbl().addNewTblPr().addNewTblW();
        tblWidth.setW(java.math.BigInteger.valueOf(5000));
        tblWidth.setType(org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.PCT);

        // 填充表头
        XWPFTableRow headerRow = table.getRow(0);
        for (int i = 0; i < cols; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            XWPFParagraph para = cell.addParagraph();
            XWPFRun run = para.createRun();
            run.setText(headers[i]);
            run.setBold(true);
        }

        // 填充数据行
        for (int i = 0; i < rows; i++) {
            XWPFTableRow row = table.getRow(i + 1);
            for (int j = 0; j < cols; j++) {
                String cellValue = (j < data[i].length) ? data[i][j] : "";
                row.getCell(j).setText(cellValue);
            }
        }

        // 添加段后间距
        XWPFParagraph spacingPara = document.createParagraph();
        spacingPara.setSpacingAfter(200);
    }

    /**
     * 生成唯一的文件名
     */
    private String generateFilename(String title) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
            
        // 清理标题中的非法字符
        String safeTitle = title.replaceAll("[^\\w\\u4e00-\\u9fa5]", "_");
        if (safeTitle.length() > 30) {
            safeTitle = safeTitle.substring(0, 30);
        }
            
        return safeTitle + "_" + timestamp + ".docx";
    }
        
    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        return "dotx";
    }

    /**
     * 获取生成的文件对象
     *
     * @param filename 文件名
     * @return 文件对象
     */
    public File getGeneratedFile(String filename) {
        Path filePath = Paths.get(fileConfig.getOutputPath(), filename);
        return filePath.toFile();
    }
}
