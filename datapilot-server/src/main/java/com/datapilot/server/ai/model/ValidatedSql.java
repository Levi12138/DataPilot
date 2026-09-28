package com.datapilot.server.ai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class ValidatedSql {
    /**
     * 已经通过安全校验的SQL
     */
    private final String sql;

    /**
     * SQL实际使用的表
     */
    private final Set<String> tables;
}
