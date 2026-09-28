package com.datapilot.server.ai.sql;

import com.datapilot.common.exception.BusinessException;
import com.datapilot.server.ai.model.GeneratedSql;
import com.datapilot.server.ai.service.impl.AiModelServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SqlGenerator {
    private final AiModelServiceImpl aiModelService;

    public GeneratedSql generate(String schemaContext,String question){

        String systemPrompt= """
                你是一个专业的 MySQL SQL 生成器。
                
                你的任务是根据提供的数据库 Schema，
                将用户的自然语言问题转换成一条可执行的查询 SQL。
                
                必须遵守以下规则：
                
                1. 只能生成 SELECT 查询。
                2. 只能使用提供的表和字段。
                3. 禁止猜测或创造不存在的表和字段。
                4. 使用 MySQL 语法。
                5. 尽量避免使用 SELECT *。
                6. 如果涉及日期范围，必须使用明确的日期边界。
                7. 如果普通明细查询可能返回大量数据，应添加合理的 LIMIT。
                8. 聚合查询如 COUNT、SUM、AVG 不要因为 LIMIT 改变统计结果。
                9. SQL 必须能够直接执行，不要添加 Markdown 代码块。
                10. tables 字段必须列出 SQL 实际使用的所有表。
                11. description 用简短中文说明 SQL 的用途。
                
                不允许生成 INSERT、UPDATE、DELETE、DROP、
                ALTER、CREATE、TRUNCATE、CALL 等操作。
                """;
        String userPrompt= """
                数据库类型：
                MySQL
                
                数据库Schema:
                %s
                
                用户问题：
                %s
                """.formatted(schemaContext,question);

        GeneratedSql result=aiModelService.chat(
                systemPrompt,
                userPrompt,
                GeneratedSql.class
        );

        if(result==null || result.getSql()==null || result.getSql().isBlank()){
            throw new BusinessException("AI生成SQL失败");
        }

        return result;
    }
}
