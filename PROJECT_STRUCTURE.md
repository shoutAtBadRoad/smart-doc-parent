# 智能文档生成系统 - 项目结构说明

## 📁 完整目录结构

```
smart-doc-parent/
│
├── pom.xml                                    # 父工程 POM 配置
├── README.md                                  # 项目说明文档
│
├── llm-client/                                # LLM 客户端模块
│   ├── pom.xml                                # 模块依赖配置
│   └── src/main/java/com/example/llm/client/service/
│       └── LlmService.java                    # LLM 服务接口（预留真实 API）
│
└── doc-engine/                                # 文档引擎核心模块
    ├── pom.xml                                # 模块依赖配置
    │
    └── src/main/
        ├── java/com/example/docgen/
        │   │
        │   ├── DocumentGeneratorApplication.java    # Spring Boot 启动类
        │   │
        │   ├── config/                              # 配置类目录
        │   │   └── FileConfig.java                  # 文件路径配置，自动创建目录
        │   │
        │   ├── controller/                          # REST API 控制器
        │   │   ├── TemplateController.java          # 模板管理接口
        │   │   │   ├── POST /api/template/upload    # 上传模板
        │   │   │   └── GET /api/template/list       # 获取模板列表
        │   │   │
        │   │   └── DocumentController.java          # 文档生成接口
        │   │       ├── POST /api/doc/generate       # 生成文档
        │   │       └── GET /api/doc/download/{filename}  # 下载文档
        │   │
        │   ├── model/                               # 数据模型
        │   │   └── DocumentBlock.java               # 文档块模型（H1/H2/TEXT/TABLE）
        │   │
        │   └── service/                             # 业务服务层
        │       ├── TemplateService.java             # 模板服务（上传、列表）
        │       └── DocumentBuilderService.java      # 核心文档构建服务
        │
        └── resources/
            ├── application.yml                      # 应用配置文件
            └── static/
                └── index.html                       # 前端页面（Vue+Axios）
```

## 🔧 技术栈清单

### 后端技术
- **JDK**: 1.8 (严格指定 source/target)
- **Spring Boot**: 2.7.18 (支持 JDK 1.8 的最新稳定版)
- **Maven**: 多模块项目管理
- **Apache POI**: 5.2.5 (Word 文档处理)
- **Jackson**: 2.13.5 (JSON 解析)
- **Lombok**: 1.18.30 (简化代码)

### 前端技术
- **HTML5 + CSS3**: 现代化 UI
- **原生 JavaScript**: Fetch API 异步请求
- **渐变背景 + 卡片式设计**: 美观界面

## 📦 Maven 模块依赖关系

```
smart-doc-parent (父工程)
│
├── llm-client (独立模块)
│   └── 依赖：spring-boot-starter, jackson, lombok
│
└── doc-engine (核心模块)
    └── 依赖：llm-client, spring-boot-starter-web, 
            apache-poi, jackson, lombok
```

## 🎯 核心功能实现

### 1. LLM 服务接口 (LlmService.java)
**位置**: `llm-client/src/main/java/com/example/llm/client/service/LlmService.java`

**功能**:
- 提供 `generateContent(String prompt)` 方法
- 当前返回模拟 JSON 数据
- 预留真实 API 调用接口（TODO 标记）

**JSON 数据结构**:
```json
{
  "title": "文档标题",
  "blocks": [
    {"type": "H1", "content": "一级标题"},
    {"type": "H2", "content": "二级标题"},
    {"type": "TEXT", "content": "普通段落"},
    {
      "type": "TABLE",
      "headers": ["列 1", "列 2"],
      "data": [["值 1", "值 2"]]
    }
  ]
}
```

### 2. 文件配置 (FileConfig.java)
**位置**: `doc-engine/src/main/java/com/example/docgen/config/FileConfig.java`

**功能**:
- 读取 `application.yml` 中的路径配置
- 使用 `@PostConstruct` 在启动时自动创建目录
- 提供路径访问方法

**配置项**:
```yaml
doc:
  template-path: ./templates    # 模板目录
  output-path: ./output         # 输出目录
```

### 3. 模板服务 (TemplateService.java)
**位置**: `doc-engine/src/main/java/com/example/docgen/service/TemplateService.java`

**功能**:
- `uploadTemplate(MultipartFile file)`: 上传 .docx 模板
- `listTemplates()`: 获取所有模板文件名列表
- 文件验证（仅允许 .docx 格式）
- 自动创建目录

### 4. 文档构建服务 (DocumentBuilderService.java)
**位置**: `doc-engine/src/main/java/com/example/docgen/service/DocumentBuilderService.java`

**核心流程**:
1. 调用 `LlmService.generateContent(prompt)` 获取 JSON
2. Jackson 解析 JSON → `List<DocumentBlock>`
3. Apache POI 加载 Word 模板
4. 遍历文档块，应用样式:
   - **H1**: Heading 1 样式（24 号字，粗体）
   - **H2**: Heading 2 样式（18 号字，粗体）
   - **TEXT**: Normal 样式（12 号字，两端对齐）
   - **TABLE**: 创建表格，填充表头和数据，设置背景色
5. 保存生成的文档到输出目录
6. 返回唯一文件名（包含时间戳）

**关键代码段**:
```java
// 处理文档块
private void processDocumentBlocks(XWPFDocument document, List<DocumentBlock> blocks) {
    for (DocumentBlock block : blocks) {
        switch (block.getType()) {
            case "H1": createHeading1Paragraph(document, block.getContent()); break;
            case "H2": createHeading2Paragraph(document, block.getContent()); break;
            case "TEXT": createNormalParagraph(document, block.getContent()); break;
            case "TABLE": createTable(document, block.getHeaders(), block.getData()); break;
        }
    }
}
```

### 5. REST API 控制器

#### TemplateController.java
**位置**: `doc-engine/src/main/java/com/example/docgen/controller/TemplateController.java`

**接口**:
- `POST /api/template/upload`: 上传模板文件
- `GET /api/template/list`: 查询模板列表

**响应格式**:
```json
{
  "success": true/false,
  "filename/templates": "...",
  "message": "..."
}
```

#### DocumentController.java
**位置**: `doc-engine/src/main/java/com/example/docgen/controller/DocumentController.java`

**接口**:
- `POST /api/doc/generate`: 生成文档
  - 请求体：`{"prompt": "主题", "templateName": "模板.docx"}`
  - 响应：`{"filename": "生成的文件名.docx"}`
  
- `GET /api/doc/download/{filename}`: 下载文档
  - 返回：文件流（attachment 下载）

### 6. 前端页面 (index.html)
**位置**: `doc-engine/src/main/resources/static/index.html`

**功能**:
- 美观的渐变背景 + 卡片式设计
- 表单输入：文档主题（textarea）、模板选择（select）
- 自动生成按钮（带 loading 动画）
- 实时消息提示（成功/错误）
- 下载链接直接跳转
- 页面加载自动获取模板列表

**UI 特性**:
- 响应式布局
- 悬停动画效果
- 10 秒自动消失的成功提示
- 移动端友好

## 🚀 快速启动步骤

### 1. 环境准备
```bash
# 检查 JDK 版本
java -version  # 必须 1.8

# 检查 Maven
mvn -version
```

### 2. 编译项目
```bash
cd smart-doc-parent
mvn clean install
```

### 3. 运行应用
```bash
cd doc-engine
mvn spring-boot:run
```

或运行主类：`DocumentGeneratorApplication`

### 4. 访问系统
浏览器打开：http://localhost:8080

## 📋 API 使用示例

### 上传模板
```bash
curl -X POST http://localhost:8080/api/template/upload \
  -F "file=@template.docx"
```

### 获取模板列表
```bash
curl http://localhost:8080/api/template/list
```

### 生成文档
```bash
curl -X POST http://localhost:8080/api/doc/generate \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "人工智能技术发展报告",
    "templateName": "template.docx"
  }'
```

### 下载文档
```bash
curl http://localhost:8080/api/doc/download/人工智能技术发展报告_20260327_123456.docx \
  --output downloaded.docx
```

## ⚙️ 配置说明

### application.yml 完整配置
```yaml
server:
  port: 8080

spring:
  application:
    name: smart-doc-engine
  
# 文件上传限制
servlet:
  multipart:
    max-file-size: 10MB
    max-request-size: 10MB

# 文档生成路径
doc:
  template-path: ./templates
  output-path: ./output
```

## 🔍 关键实现细节

### 1. JDK 1.8 兼容性保证
父工程 pom.xml 明确指定：
```xml
<properties>
    <java.version>1.8</java.version>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 2. 跨域访问支持
所有 Controller 添加注解：
```java
@CrossOrigin(origins = "*")
```

### 3. 文件路径适配
使用 `Paths.get()` 自动适配 Windows/Linux:
```java
Path templatePath = Paths.get(fileConfig.getTemplatePath(), templateName);
```

### 4. 唯文件名生成
包含标题 + 时间戳，防止重复：
```java
private String generateFilename(String title) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String timestamp = sdf.format(new Date());
    String safeTitle = title.replaceAll("[^\\w\\u4e00-\\u9fa5]", "_");
    return safeTitle + "_" + timestamp + ".docx";
}
```

### 5. Apache POI 表格样式
```java
// 设置表格宽度 100%
CTTblWidth tblWidth = tblPr.addNewTblW();
tblWidth.setW(org.apache.poi.util.Units.toTwips(500));
tblWidth.setType(STTblWidth.PCT);

// 表头背景色（浅灰色）
cell.setColor("E0E0E0");

// 单元格居中
para.setAlignment(ParagraphAlignment.CENTER);
```

## 🎨 前端界面预览

```
┌─────────────────────────────────────┐
│   📄 智能文档生成系统                 │
│   基于 AI 技术的自动化文档生成工具     │
├─────────────────────────────────────┤
│                                     │
│  文档主题/提示词                     │
│  ┌─────────────────────────────┐   │
│  │ 请输入您想要生成的文档主题... │   │
│  └─────────────────────────────┘   │
│                                     │
│  选择模板                            │
│  ┌─────────────────────────────┐   │
│  │ -- 请选择模板 --            ▼ │   │
│  └─────────────────────────────┘   │
│  提示：可先上传 Word 模板文件          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │       生成文档              │   │
│  └─────────────────────────────┘   │
│                                     │
│  ✓ 文档生成成功！                   │
│    文件名：xxx_20260327_123456.docx │
│    [⬇️ 点击下载文档]                 │
└─────────────────────────────────────┘
```

## 📝 扩展开发指南

### 集成真实 LLM API
修改 `LlmService.java`:
```java
public String generateContent(String prompt) {
    // 调用 OpenAI/文心一言等 API
    String apiUrl = "https://api.openai.com/v1/chat/completions";
    // ... HTTP 请求代码
    return responseContent;
}
```

### 添加新文档类型
1. 在 `DocumentBlock` 中添加新 type 常量
2. 在 `DocumentBuilderService.processDocumentBlocks()` 中添加 case 分支
3. 实现对应的创建方法（如 `createImage()`, `createList()`）

### 自定义样式
修改 `DocumentBuilderService` 中的样式设置方法:
```java
private void createHeading1Paragraph(XWPFDocument document, String content) {
    XWPFParagraph paragraph = document.createParagraph();
    paragraph.setAlignment(ParagraphAlignment.CENTER); // 居中对齐
    
    XWPFRun run = paragraph.createRun();
    run.setText(content);
    run.setFontSize(28);  // 调整字号
    run.setColor("FF0000"); // 红色字体
    run.addBreak();  // 换行
}
```

## ✅ 项目完成清单

- [x] 父工程 pom.xml（依赖管理、JDK 1.8 配置）
- [x] llm-client 模块（LlmService 预留接口）
- [x] doc-engine 模块 pom.xml（依赖配置）
- [x] application.yml（端口、路径配置）
- [x] FileConfig（自动创建目录）
- [x] DocumentBlock 模型类
- [x] TemplateService + TemplateController
- [x] DocumentBuilderService + DocumentController
- [x] DocumentGeneratorApplication 启动类
- [x] index.html 前端页面
- [x] README.md 使用文档

## 🎉 项目亮点

1. **严格的 JDK 1.8 兼容**: 所有配置确保在 JDK 1.8 环境运行
2. **模块化设计**: Maven 多模块清晰分离职责
3. **预留扩展接口**: LLM 服务易于替换为真实 API
4. **完整的错误处理**: 每个接口都有 try-catch 和友好提示
5. **美观的前端界面**: 现代化设计，用户体验良好
6. **自动目录管理**: 启动时自动创建所需目录
7. **详细的注释文档**: 每个类和方法都有清晰的中文注释

---

**项目已就绪！可以立即运行使用。** 🚀
