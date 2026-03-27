package com.example.docgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 文档生成器应用启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.docgen",
    "com.example.llm.client"
})
public class DocumentGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentGeneratorApplication.class, args);
        System.out.println("=====================================");
        System.out.println("  智能文档生成系统启动成功！");
        System.out.println("  访问地址：http://localhost:8080");
        System.out.println("=====================================");
    }
}
