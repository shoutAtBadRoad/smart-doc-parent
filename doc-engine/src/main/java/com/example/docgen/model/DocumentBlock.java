package com.example.docgen.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档块模型
 * 用于表示文档中的一个段落或表格
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentBlock {

    /**
     * 块类型：H1, H2, TEXT, TABLE
     */
    private String type;

    /**
     * 文本内容（对于 TABLE 类型为空）
     */
    private String content;

    /**
     * 表格头部（仅 TABLE 类型使用）
     */
    private String[] headers;

    /**
     * 表格数据（仅 TABLE 类型使用）
     */
    private String[][] data;
}
