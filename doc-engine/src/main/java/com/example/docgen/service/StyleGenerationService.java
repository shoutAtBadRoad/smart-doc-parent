package com.example.docgen.service;

import com.example.llm.client.service.LlmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 样式生成服务 - 通过自然语言生成 Word 样式定义
 */
@Service
public class StyleGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(StyleGenerationService.class);

    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    public StyleGenerationService(LlmService llmService) {
        this.llmService = llmService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 通过自然语言描述生成 Word 样式定义
     * @param styleDescription 自然语言描述的样式要求
     * @return 生成的样式定义 JSON
     */
    public String generateStyleDefinition(String styleDescription) {
        logger.info("开始生成样式定义：{}", styleDescription);

        String prompt = String.format(
            "请根据以下描述生成 Word 文档样式定义，要求返回标准 JSON 格式：\n" +
            "描述：%s\n\n" +
            "JSON 结构要求：\n" +
            "{\n" +
            "  \"styles\": [\n" +
            "    {\n" +
            "      \"name\": \"样式名称\",\n" +
            "      \"type\": \"样式类型（paragraph/table/run）\",\n" +
            "      \"properties\": {\n" +
            "        \"fontFamily\": \"字体\",\n" +
            "        \"fontSize\": \"字号 (数字)\",\n" +
            "        \"bold\": true/false,\n" +
            "        \"italic\": true/false,\n" +
            "        \"underline\": \"下划线类型\",\n" +
            "        \"color\": \"颜色 (如：#000000)\",\n" +
            "        \"alignment\": \"对齐方式 (left/center/right/both)\",\n" +
            "        \"spacing\": \"行距 (数字)\",\n" +
            "        \"indentation\": \"缩进 (字符数)\",\n" +
            "        \"backgroundColor\": \"背景色\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "注意：只返回 JSON，不要有其他文字说明。", 
            styleDescription);

        try {
            String jsonContent = llmService.generateContent(prompt);
            logger.info("生成的样式定义：{}", jsonContent);
            return jsonContent;
        } catch (Exception e) {
            logger.error("生成样式定义失败", e);
            throw new RuntimeException("生成样式定义失败：" + e.getMessage(), e);
        }
    }

    /**
     * 解析样式 JSON 为对象列表
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> parseStylesJson(String jsonContent) {
        try {
            Map<String, Object> root = objectMapper.readValue(jsonContent, Map.class);
            return (List<Map<String, Object>>) root.get("styles");
        } catch (Exception e) {
            logger.error("解析样式 JSON 失败", e);
            throw new RuntimeException("解析样式 JSON 失败：" + e.getMessage(), e);
        }
    }
}
