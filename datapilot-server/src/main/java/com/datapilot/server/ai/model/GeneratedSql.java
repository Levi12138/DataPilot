package com.datapilot.server.ai.model;

import lombok.Data;

import java.util.List;

@Data
public class GeneratedSql {
    /**
     * AI生成的SQL
     */
    private String sql;

    /**
     * SQL涉及的表
     */
    private List<String> tables;

    /**
     * SQL含义说明
     */
    private String description;
}
