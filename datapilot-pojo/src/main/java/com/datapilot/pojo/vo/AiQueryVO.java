package com.datapilot.pojo.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiQueryVO {
    /**
     * AI生成的自然语言答案
     */
    private String answer;

    /**
     * 实际执行的SQL
     */
    private String sql;

    /**
     * 查询结果列名
     */
    private List<String> columns;

    /**
     * 查询结果
     */
    private List<Map<String, Object>> rows;

    /**
     * 返回行数
     */
    private Integer rowCount;

    /**
     * 整个问数过程耗时
     */
    private Long elapsedMs;
}
