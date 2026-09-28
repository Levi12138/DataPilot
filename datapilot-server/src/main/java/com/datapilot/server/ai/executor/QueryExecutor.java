package com.datapilot.server.ai.executor;

import com.datapilot.common.exception.BusinessException;
import com.datapilot.server.ai.model.ValidatedSql;
import com.datapilot.server.datasource.ExternalDataSourceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryExecutor {
    /**
     * 单次查询最大执行时间，单位为秒
     */
    private static final int QUERY_TIMEOUT_SECONDS=10;

    /**
     * 最大返回行数
     */
    private static final int MAX_ROWS=1000;

    private final ExternalDataSourceManager dataSourceManager;

    public List<Map<String,Object>> execute(Long datasourceId, ValidatedSql validatedSql){
        if(validatedSql==null || validatedSql.getSql()==null || validatedSql.getSql().isBlank()){
            throw new BusinessException("待执行SQL不能为空");
        }

        try{
            //根据datasourceId获取外部数据源连接池
            DataSource dataSource=dataSourceManager.getDataSource(datasourceId);

            //为当前数据源创建JdbcTemplate
            JdbcTemplate jdbcTemplate=new JdbcTemplate(dataSource);

            //设置查询保护
            jdbcTemplate.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            jdbcTemplate.setMaxRows(MAX_ROWS);

            //执行通过校验的SQL
            return jdbcTemplate.queryForList(validatedSql.getSql());
        }catch (BusinessException e){
            throw e;
        }catch (Exception e){
            throw new BusinessException("SQL执行失败"+e.getMessage());
        }
    }
}
