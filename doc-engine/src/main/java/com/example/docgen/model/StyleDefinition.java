package com.example.docgen.model;

import java.util.Map;

/**
 * 样式定义模型
 */
public class StyleDefinition {
    private String name;
    private String type; // paragraph, table, run
    private Map<String, Object> properties;

    public StyleDefinition() {
    }

    public StyleDefinition(String name, String type, Map<String, Object> properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
