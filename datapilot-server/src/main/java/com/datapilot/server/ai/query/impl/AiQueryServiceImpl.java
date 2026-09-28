package com.datapilot.server.ai.query.impl;

import com.datapilot.common.enums.QueryState;
import com.datapilot.pojo.dto.AiQueryDTO;
import com.datapilot.pojo.entity.QueryHistory;
import com.datapilot.pojo.vo.AiQueryVO;
import com.datapilot.server.ai.answer.AnswerGenerator;
import com.datapilot.server.ai.executor.QueryExecutor;
import com.datapilot.server.ai.executor.QueryResultProcessor;
import com.datapilot.server.ai.model.GeneratedSql;
import com.datapilot.server.ai.model.ValidatedSql;
import com.datapilot.server.ai.query.AiQueryService;
import com.datapilot.server.ai.schema.SchemaContextBuilder;
import com.datapilot.server.ai.sql.SqlGenerator;
import com.datapilot.server.ai.sql.SqlValidator;
import com.datapilot.server.mapper.QueryHistoryMapper;
import com.datapilot.server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiQueryServiceImpl implements AiQueryService {

    private final SqlGenerator sqlGenerator;
    private final SchemaContextBuilder contextBuilder;
    private final SqlValidator sqlValidator;
    private final QueryExecutor queryExecutor;
    private final AnswerGenerator answerGenerator;
    private final QueryHistoryMapper historyMapper;
    private final QueryResultProcessor processor;

    @Value("${spring.ai.openai.chat.options.model:unknown}")
    private String modelName;



    @Override
    public AiQueryVO execute(AiQueryDTO aiQueryDTO){
        //获取请求发起时间
        long startTime=System.nanoTime();

        System.out.println("DTO = " + aiQueryDTO);
        System.out.println("datasourceId = " + aiQueryDTO.getDatasourceId());

        Long datasourceId=aiQueryDTO.getDatasourceId();
        String question=aiQueryDTO.getQuestion();

        //构建表描述Schema
        String schemaContext=contextBuilder.build(datasourceId);

        //根据用户的问题，生成SQL
        GeneratedSql generatedSql;
        try{
            generatedSql=sqlGenerator.generate(schemaContext,question);
        }catch(Exception e){
            saveFailureHistory(
                    datasourceId,
                    question,
                    null,
                    QueryState.SQL_GENERATION_FAILED,
                    e.getMessage(),
                    startTime
            );

            throw e;
        }

        //校验SQL
        ValidatedSql validatedSql;
        try{
            validatedSql=sqlValidator.validate(datasourceId, generatedSql);
        }catch(Exception e){
            saveFailureHistory(
                    datasourceId,
                    question,
                    generatedSql.getSql(),
                    QueryState.SQL_REJECTED,
                    e.getMessage(),
                    startTime
            );

            throw e;
        }

        //执行SQL
        List<Map<String,Object>> rows;
        try{
            rows=queryExecutor.execute(datasourceId,validatedSql);

            rows=processor.process(rows);
        }catch(Exception e){
            saveFailureHistory(
                    datasourceId,
                    question,
                    validatedSql.getSql(),
                    QueryState.QUERY_FAILED,
                    e.getMessage(),
                    startTime
            );

            throw e;
        }


        //获取AI的回答
        String answer;
        try{
            answer=answerGenerator.generate(question,validatedSql.getSql(),rows);
        }catch(Exception e){
            saveFailureHistory(
                    datasourceId,
                    question,
                    validatedSql.getSql(),
                    QueryState.ANSWER_FAILED,
                    e.getMessage(),
                    startTime
            );

            throw e;
        }


        //提取列名
        List<String> columns=extractColumns(rows);

        //计算整个过程的耗时
        long elapsedMs=(System.nanoTime()-startTime)/1_000_000;

        AiQueryVO aiQueryVO=new AiQueryVO();
        aiQueryVO.setAnswer(answer);
        aiQueryVO.setRows(rows);
        aiQueryVO.setSql(validatedSql.getSql());
        aiQueryVO.setElapsedMs(elapsedMs);
        aiQueryVO.setRowCount(rows.size());
        aiQueryVO.setColumns(columns);

        saveSuccessHistory(datasourceId,question, validatedSql.getSql(),rows.size(),elapsedMs);

        return aiQueryVO;
    }


    /**
     * 从查询结果中提取列名
     */
    private List<String> extractColumns(List<Map<String,Object>> rows){
        if(rows==null || rows.isEmpty()){
            return new ArrayList<>();
        }
        return new ArrayList<>(
                rows.get(0).keySet()
        );
    }

    /**
     * 问数成功，向历史查询表中插入一条成功记录
     */
    private void saveSuccessHistory(Long datasourceId,String question,String sql,Integer rowCount,Long elapsedMs){
        QueryHistory queryHistory=new QueryHistory();

        queryHistory.setUserId(SecurityUtils.getCurrentId());

        queryHistory.setDatasourceId(datasourceId);

        queryHistory.setQuestion(question);

        queryHistory.setGeneratedSql(sql);

        queryHistory.setRowCount(rowCount);

        queryHistory.setElapsedMs(elapsedMs);

        queryHistory.setModelName(modelName);

        queryHistory.setErrorMessage(null);

        queryHistory.setStatus(QueryState.SUCCESS.name());

        historyMapper.insert(queryHistory);

    }

    /**
     * 问数失败，将失败日志插入历史查询表
     */
    private void saveFailureHistory(Long datasourceId,String question,String sql,QueryState status,String errorMessage,long startTime){
        try{
            long elapsedMs=(System.nanoTime()-startTime)/1_000_000;

            QueryHistory queryHistory=new QueryHistory();

            queryHistory.setUserId(SecurityUtils.getCurrentId());

            queryHistory.setDatasourceId(datasourceId);

            queryHistory.setQuestion(question);

            queryHistory.setGeneratedSql(sql);

            queryHistory.setStatus(status.name());

            queryHistory.setErrorMessage(errorMessage);

            queryHistory.setRowCount(null);

            queryHistory.setElapsedMs(elapsedMs);

            queryHistory.setModelName(modelName);

            historyMapper.insert(queryHistory);
        }catch(Exception historyException){
            log.error(
                    "保存查询失败记录异常，原始状态{}",
                    status,
                    historyException
            );
        }
    }

}
