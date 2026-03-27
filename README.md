# 智能文档生成系统 - 快速开始指南

## 项目结构

```
smart-doc-parent/
├── pom.xml                          # 父工程 POM
├── llm-client/                      # LLM 客户端模块
│   ├── pom.xml
│   └── src/main/java/com/example/llm/client/service/
│       └── LlmService.java          # LLM 服务接口（预留）
└── doc-engine/                      # 文档引擎核心模块
    ├── pom.xml
    └── src/main/
        ├── java/com/example/docgen/
        │   ├── DocumentGeneratorApplication.java  # 启动类
        │   ├── config/
        │   │   └── FileConfig.java               # 文件配置类
        │   ├── controller/
        │   │   ├── TemplateController.java       # 模板管理接口
        │   │   └── DocumentController.java       # 文档生成接口
        │   ├── model/
        │   │   └── DocumentBlock.java            # 文档块模型
        │   └── service/
        │       ├── TemplateService.java          # 模板服务
        │       └── DocumentBuilderService.java   # 文档构建核心服务
        └── resources/
            ├── application.yml                   # 应用配置
            └── static/
                └── index.html                    # 前端页面
```

## 环境要求

- **JDK**: 1.8 (已配置 maven-compiler-plugin source/target 1.8)
- **Maven**: 3.6+
- **Spring Boot**: 2.7.18

## 快速开始

### 1. 编译项目

在项目根目录执行：

```bash
mvn clean install
```

### 2. 运行应用

进入 doc-engine 模块：

```bash
cd doc-engine
mvn spring-boot:run
```

或者直接运行主类：`DocumentGeneratorApplication`

### 3. 访问系统

浏览器打开：http://localhost:8080

## API 接口说明

### 1. 模板管理

#### 上传模板
- **接口**: `POST /api/template/upload`
- **参数**: `file` (Word 模板文件，.docx 格式)
- **响应**: 
```json
{
  "success": true,
  "filename": "template.docx",
  "message": "模板上传成功"
}
```

#### 获取模板列表
- **接口**: `GET /api/template/list`
- **响应**:
```json
{
  "success": true,
  "templates": ["template1.docx", "template2.docx"],
  "count": 2
}
```

### 2. 文档生成

#### 生成文档
- **接口**: `POST /api/doc/generate`
- **请求体**:
```json
{
  "prompt": "人工智能技术发展报告",
  "templateName": "template.docx"
}
```
- **响应**:
```json
{
  "success": true,
  "filename": "人工智能技术发展报告_20260327_123456.docx",
  "message": "文档生成成功"
}
```

#### 下载文档
- **接口**: `GET /api/doc/download/{filename}`
- **返回**: 文件下载流

## 核心功能说明

### 1. LLM 服务接口（LlmService.java）

当前为模拟实现，返回预设的 JSON 数据。预留了真实 API 调用接口：

```java
public String generateContent(String prompt) {
    // TODO: 调用真实的大模型 API
    return "模拟的 JSON 数据...";
}
```

JSON 数据结构：
```json
{
  "title": "文档标题",
  "blocks": [
    {
      "type": "H1",
      "content": "一级标题内容"
    },
    {
      "type": "H2",
      "content": "二级标题内容"
    },
    {
      "type": "TEXT",
      "content": "普通段落文本"
    },
    {
      "type": "TABLE",
      "headers": ["列 1", "列 2", "列 3"],
      "data": [
        ["数据 1", "数据 2", "数据 3"],
        ["数据 4", "数据 5", "数据 6"]
      ]
    }
  ]
}
```

### 2. 文档构建服务（DocumentBuilderService.java）

核心处理流程：
1. 调用 LLM 服务获取 JSON 内容
2. 使用 Jackson 解析 JSON 为 `List<DocumentBlock>`
3. 使用 Apache POI 加载 Word 模板
4. 遍历文档块：
   - **H1**: 创建一级标题（粗体，24 号字）
   - **H2**: 创建二级标题（粗体，18 号字）
   - **TEXT**: 创建普通段落（两端对齐，12 号字）
   - **TABLE**: 创建表格并填充数据
5. 保存生成的文档到输出目录

### 3. 文件配置（FileConfig.java）

自动创建目录：
- 读取 `application.yml` 中的 `doc.template-path` 和 `doc.output-path`
- 应用启动时自动检查并创建这两个目录

## 配置说明

### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: smart-doc-engine
  
# 文件上传配置
servlet:
  multipart:
    max-file-size: 10MB
    max-request-size: 10MB

# 文档生成配置
doc:
  template-path: ./templates    # 模板存放路径
  output-path: ./output         # 生成文件存放路径
```

## 依赖管理

### 主要依赖版本（父工程 pom.xml 统一管理）

- Spring Boot: 2.7.18
- Apache POI: 5.2.5
- Jackson: 2.13.5
- Lombok: 1.18.30

### Maven 编译配置

```xml
<maven-compiler-plugin>
    <version>3.11.0</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <encoding>UTF-8</encoding>
    </configuration>
</maven-compiler-plugin>
```

## 使用说明

### 第一步：准备模板

1. 准备一个 Word 模板文件（.docx 格式）
2. 通过前端页面或 API 上传到系统
3. 模板将保存在 `./templates` 目录

### 第二步：生成文档

1. 在前端页面输入文档主题（提示词）
2. 选择已上传的模板
3. 点击"生成文档"按钮
4. 系统调用 LLM 生成内容并应用到模板
5. 生成的文档保存在 `./output` 目录
6. 点击下载链接获取文档

## 扩展开发建议

### 1. 集成真实 LLM API

修改 `LlmService.java`:

```java
public String generateContent(String prompt) {
    // 示例：调用 OpenAI API
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer YOUR_API_KEY");
    
    Map<String, Object> body = new HashMap<>();
    body.put("model", "gpt-3.5-turbo");
    body.put("messages", Arrays.asList(
        Map.of("role", "user", "content", prompt)
    ));
    
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
    ResponseEntity<String> response = restTemplate.postForEntity(
        "https://api.openai.com/v1/chat/completions",
        request,
        String.class
    );
    
    return parseResponse(response.getBody());
}
```

### 2. 添加更多文档元素类型

在 `DocumentBlock` 中添加新类型：
- IMAGE: 图片
- LIST: 列表
- CODE: 代码块

在 `DocumentBuilderService.processDocumentBlocks()` 中添加对应处理逻辑。

### 3. 支持更多模板格式

目前仅支持 .docx，可扩展支持：
- .doc（旧版 Word）
- PDF
- Markdown

## 注意事项

1. **JDK 版本**: 必须使用 JDK 1.8，已在 pom.xml 中明确指定
2. **编码**: 所有文件使用 UTF-8 编码
3. **路径**: Windows 和 Linux 路径分隔符不同，代码中使用 `Paths.get()` 自动适配
4. **样式**: Apache POI 对 Word 样式控制有限，主要通过格式设置模拟样式

## 故障排查

### 常见问题

1. **端口被占用**
   - 修改 `application.yml` 中的 `server.port`

2. **模板目录不存在**
   - 检查 `FileConfig` 是否正常执行
   - 手动创建 `./templates` 和 `./output` 目录

3. **中文乱码**
   - 确保文件保存为 UTF-8 编码
   - 检查系统文件编码设置

4. **Maven 编译错误**
   - 确认 JDK 版本为 1.8
   - 执行 `mvn clean install -U` 强制更新依赖

## 后续优化方向

1. 集成真实的大模型 API（OpenAI、文心一言等）
2. 支持自定义文档结构定义
3. 添加文档预览功能
4. 支持批量文档生成
5. 添加用户权限管理
6. 实现文档版本控制
