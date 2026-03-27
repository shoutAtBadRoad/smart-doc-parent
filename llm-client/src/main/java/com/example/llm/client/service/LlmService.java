package com.example.llm.client.service;

import org.springframework.stereotype.Service;

/**
 * LLM 服务接口实现
 * 用于调用大语言模型生成文档内容
 */
@Service
public class LlmService {

    /**
     * 根据提示词生成内容
     *
     * @param prompt 提示词
     * @return 生成的 JSON 格式内容字符串
     */
    public String generateContent(String prompt) {
        // TODO: 预留真实 API 调用接口
        // 目前返回模拟的 JSON 数据用于测试

//        return "10113311313";

        return "{\n" +
                "  \"title\": \"人工智能技术发展报告\",\n" +
                "  \"blocks\": [\n" +
                "    {\n" +
                "      \"type\": \"H1\",\n" +
                "      \"content\": \"人工智能技术概述\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"TEXT\",\n" +
                "      \"content\": \"人工智能（AI）是计算机科学的一个重要分支，它试图理解智能的实质，并生产出一种新的能以人类智能相似的方式做出反应的智能机器。\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"H2\",\n" +
                "      \"content\": \"主要应用领域\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"TABLE\",\n" +
                "      \"headers\": [\"领域\", \"应用场景\", \"典型案例\"],\n" +
                "      \"data\": [\n" +
                "        [\"医疗\", \"疾病诊断\", \"IBM Watson\"],\n" +
                "        [\"金融\", \"风险评估\", \"智能投顾\"],\n" +
                "        [\"交通\", \"自动驾驶\", \"Tesla Autopilot\"]\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"H2\",\n" +
                "      \"content\": \"未来发展趋势\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"TEXT\",\n" +
                "      \"content\": \"随着深度学习、强化学习等技术的不断进步，人工智能将在更多领域展现其强大能力。预计未来 AI 将更加注重与人类的协作，而非简单的替代。\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}
