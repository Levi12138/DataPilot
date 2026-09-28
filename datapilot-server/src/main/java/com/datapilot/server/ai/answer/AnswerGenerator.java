package com.datapilot.server.ai.answer;

import com.datapilot.common.exception.BusinessException;
import com.datapilot.server.ai.service.impl.AiModelServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnswerGenerator {
    private final AiModelServiceImpl aiModelService;

    public String generate(String question, String sql, List<Map<String,Object>> rows){

        if (question == null || question.isBlank()) {
            throw new BusinessException("问题不能为空");
        }

        if (sql == null || sql.isBlank()) {
            throw new BusinessException("SQL不能为空");
        }

        if (rows == null) {
            throw new BusinessException("查询结果不能为空");
        }

        //如果结果为空，直接返回，不用调大模型
        if(rows.isEmpty()){
            return "没有查询到符合条件的数据";
        }

        String systemPrompt = """
                你是一个企业数据分析助手。

                你的任务是根据用户问题、执行SQL以及查询结果，
                生成简洁、准确、可读的中文回答。

                必须遵守以下规则：
                1. 只能依据提供的查询结果回答。
                2. 不得虚构任何数字、结论或趋势。
                3. 如果查询结果无法支持用户问题，应明确说明数据不足。
                4. 回答尽量简洁清晰，避免冗长。
                5. 如果结果中有明确统计值，可直接用自然语言表述。
                6. 不要暴露无关技术细节，不要输出“根据SQL可知”之类生硬措辞。
                """;

        String userPrompt = """
                用户问题：
                %s

                执行SQL：
                %s

                查询结果：
                %s
                """.formatted(question, sql, rows.toString());

        String answer= aiModelService.chat(
                systemPrompt,userPrompt
        );

        if(answer==null || answer.isBlank()){
            throw new BusinessException("AI生成回答失败");
        }

        return answer.trim();

    }

}
