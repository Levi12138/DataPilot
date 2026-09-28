package com.datapilot.server.ai.sql;

import com.datapilot.common.exception.BusinessException;
import com.datapilot.pojo.entity.MetadataColumn;
import com.datapilot.pojo.entity.MetadataTable;
import com.datapilot.server.ai.model.GeneratedSql;
import com.datapilot.server.ai.model.ValidatedSql;
import com.datapilot.server.mapper.MetadataColumnMapper;
import com.datapilot.server.mapper.MetadataTableMapper;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.stereotype.Component;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.statement.select.Limit;

import net.sf.jsqlparser.expression.Expression;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SqlValidator {
    private static final long DEFAULT_LIMIT=200L;
    private static final long MAX_LIMIT=1000L;

    private final MetadataTableMapper tableMapper;
    private final MetadataColumnMapper columnMapper;

    public ValidatedSql validate(Long datasourceId, GeneratedSql generatedSql){
        if(generatedSql==null || generatedSql.getSql()==null || generatedSql.getSql().isBlank()){
            throw new BusinessException("SQL不能为空");
        }

        String sql=generatedSql.getSql().trim();

        try{
            Statements statements = CCJSqlParserUtil.parseStatements(sql);

            if(statements==null | statements.isEmpty()){
                throw new BusinessException("SQL不能为空");
            }
            //只允许执行一条SQL
            if(statements.size()!=1){
                throw new BusinessException("禁止执行多条SQL");
            }

            Statement statement=statements.get(0);

            System.out.println("Statement:"+statement);

            //只允许执行SELECT
            if(!(statement instanceof Select)){
                throw new BusinessException("只允许执行Select查询");
            }

            //从SQL AST中提取真正使用的表
            Set<String> usedTables= TablesNamesFinder.findTables(sql);

            if(usedTables==null || usedTables.isEmpty()){
                throw new BusinessException("SQL中未找到可查询的业务表");
            }

            //查询当前数据源允许访问的表
            List<MetadataTable> metadataTables=tableMapper.selectByDatasourceId(datasourceId);

            if(metadataTables==null || metadataTables.isEmpty()){
                throw new BusinessException("该数据源暂无元数据，请先同步");
            }

            Set<String> allowTables=buildAllowedTables(metadataTables);

            //表名白名单校验
            for(String table:usedTables){
                String normalizedTable=normalize(table);

                if(!allowTables.contains(normalizedTable)){
                    throw new BusinessException("SQL使用了未授权或不存在的表"+table);
                }

            }

            //字段白名单校验
            PlainSelect plainSelect=((Select) statement).getPlainSelect();

            Set<String> allowColumns=buildAllowedColumns(metadataTables,usedTables);

            Set<String> selectAliases=extractSelectAliases(plainSelect);

            validateColumns(plainSelect,allowColumns,selectAliases);

            applyLimitStrategy(plainSelect);


            /**
             * 使用Parser重新生成SQL
             * 此时已确认它只有一条语句且是SELECT
             */
            String validatedSql=statement.toString();

            return new ValidatedSql(validatedSql,usedTables);

        } catch (JSQLParserException e){
            throw new BusinessException("SQL解析失败");
        }
    }

    /**
     * 表白名单
     * @param metadataTables
     * @return
     */
    private Set<String> buildAllowedTables(List<MetadataTable> metadataTables){
        Set<String> allowedTables=new HashSet<>();

        for(MetadataTable table:metadataTables){
            String tableName=normalize(table.getTableName());

            allowedTables.add(tableName);

            /*
             *如果有schemaName,同时允许
             * 如hotel_bi.orders
             */
            if(table.getSchemaName()!=null && !table.getSchemaName().isBlank()){
                String fullName=normalize(table.getSchemaName())+"."+tableName;

                allowedTables.add(fullName);
            }
        }
        return allowedTables;
    }

    /**
     * 字段白名单
     */
    private Set<String> buildAllowedColumns(List<MetadataTable> tables,Set<String> usedTables){
        Set<String> allowedColumns=new HashSet<>();

        //SQL实际用到的表先统一格式
        Set<String> normalizedUsedTables=new HashSet<>();

        for(String usedTable:usedTables){
            normalizedUsedTables.add(normalize(usedTable));
        }

        for(MetadataTable table:tables){
            String tableName=normalize(table.getTableName());

            String fullTableName=tableName;

            if(table.getSchemaName()!=null && !table.getSchemaName().isBlank()){
                fullTableName=normalize(table.getSchemaName())+"."+tableName;
            }

            /*
             *如果SQL没有使用这张表，就不用加载它的字段
             */
            if(!normalizedUsedTables.contains(tableName) && !normalizedUsedTables.contains(fullTableName)){
                continue;
            }

            List<MetadataColumn> columns=columnMapper.selectByTableId(table.getId());

            if(columns==null){
                continue;
            }

            for(MetadataColumn column:columns){
                allowedColumns.add(column.getColumnName());
            }
        }
        return allowedColumns;
    }


    /**
     * 提取别名
     */
    private Set<String> extractSelectAliases(PlainSelect plainSelect){
        Set<String> aliases=new HashSet<>();

        for(SelectItem<?> item: plainSelect.getSelectItems()){
            Alias alias=item.getAlias();
            if(alias!=null){
                aliases.add(alias.getName().toLowerCase());
            }
        }
        return aliases;
    }

    /**
     * 从一个SQL表达式中提取字段
     *
     * 例如：
     * SUM(o.amount) ->amount
     * o.status ->status
     * h.id=o.hotel_id ->id,hotel_id
     */
    private Set<String> extractColumnsFromExpression(Expression expression){
        Set<String> columns=new HashSet<>();

        if(expression==null){
            return columns;
        }

        TablesNamesFinder<Void> finder = new TablesNamesFinder<>(){
            @Override
            public <S> Void visit(Column column,S context){
                columns.add(SqlValidator.this.normalize(column.getColumnName()));
                return null;
            }
        };
        expression.accept(finder,null);
        return columns;
    }

    /**
     * 校验一个表达式中的字段
      * @param expression     要检查的SQL表达式
     * @param allowColumns    数据库中真实字段的白名单
     * @param selectAliases   SELECT定义的别名
     * @param allowAliases    当前SQL位置是否允许使用别名
     */
    private void validateExpressionColumns(
            Expression expression,
            Set<String> allowColumns,
            Set<String> selectAliases,
            boolean allowAliases){

        Set<String> columns=extractColumnsFromExpression(expression);

        for(String column:columns){
            //第一优先级：数据库真实字段
            if(allowColumns.contains(column)){
                continue;
            }

            //第二优先级：当前区域允许使用SELECT别名
            if(allowAliases && selectAliases.contains(column)){
                continue;
            }

            throw new BusinessException("SQL使用了不存在或未授权的字段："+column);
        }
    }

    /**
     *按SQL不同位置校验
     */
    private void validateColumns(PlainSelect plainSelect,Set<String> allowColumns,Set<String> selectAliases){
        /*
         *SELECT部分，这里必须是真实数据库字段
         */
        for(SelectItem<?> item: plainSelect.getSelectItems()){
            validateExpressionColumns(item.getExpression(),allowColumns,selectAliases,false);
        }

        /*
         *WHERE部分，这里允许使用别名
         */
        validateExpressionColumns(
                plainSelect.getWhere(),
                allowColumns,
                selectAliases,
                false
        );

        /*
         *JOIN ON部分
         */
        if(plainSelect.getJoins()!=null){
            for(Join join:plainSelect.getJoins()){
                for(Expression onExpression: join.getOnExpressions()){
                    validateExpressionColumns(
                            onExpression,
                            allowColumns,
                            selectAliases,
                            false
                    );
                }

                if(join.getUsingColumns()!=null){
                    for(Column column: join.getUsingColumns()){
                        String columnName=column.getColumnName();

                        if(!allowColumns.contains(columnName)){
                            throw new BusinessException("SQL使用了不存在或未授权的字段："+columnName);
                        }
                    }
                }
            }
        }

        /*
         *GROUP BY部分，允许一定情况下使用SELECT别名
         */
        if(plainSelect.getGroupBy()!=null && plainSelect.getGroupBy().getGroupByExpressionList()!=null){
            for(Expression expression:plainSelect.getGroupBy().getGroupByExpressionList()){
                validateExpressionColumns(expression,allowColumns,selectAliases,true);
            }
        }

        /*
         *HAVING部分，允许使用别名
         */
        validateExpressionColumns(
                plainSelect.getHaving(),
                allowColumns,
                selectAliases,
                true
        );

        /*
         *ORDER BY部分，可以使用别名
         */
        if(plainSelect.getOrderByElements()!=null){
            for(OrderByElement element: plainSelect.getOrderByElements()){
                validateExpressionColumns(
                        element.getExpression(),
                        allowColumns,
                        selectAliases,
                        true
                );
            }
        }


    }

    /**
     * 判断是否为简单聚合
     * @param plainSelect
     * @return
     */
    private boolean isSimpleAggregateQuery(PlainSelect plainSelect){
        //有GROUP BY时，结果可能有很多行，不能认为是单行聚合
        if(plainSelect.getGroupBy()!=null){
            return false;
        }

        Set<String> aggregateFunctions=Set.of("count","sum","avg","min","max");

        for(SelectItem<?> item:plainSelect.getSelectItems()){
            Expression expression=item.getExpression();

            if(!(expression instanceof Function function)){
                return false;
            }

            String functionName=normalize(function.getName());

            if(!aggregateFunctions.contains(functionName)){
                return false;
            }
        }
        return true;
    }

    private void applyLimitStrategy(PlainSelect plainSelect){
        Limit limit=plainSelect.getLimit();

        /*
         *情况1：SQL自己没有limit
         */
        if(limit==null){
            //判断是否为单行聚合
            if(isSimpleAggregateQuery(plainSelect)){
                return;
            }

            Limit newLimit=new Limit();
            newLimit.setRowCount(new LongValue(DEFAULT_LIMIT));

            plainSelect.setLimit(newLimit);

            return;
        }

        /*
         *情况2：SQL本身已有Limit
         */
        Expression rowCount=limit.getRowCount();

        //只接受明确数字行数
        if(!(rowCount instanceof LongValue longValue)){
            throw new BusinessException("LIMIT必须是明确数字");
        }

        long value=longValue.getValue();

        if(value<=0){
            throw new BusinessException("LIMIT必须大于0");
        }

        //如果AI生成Limit大于我们设置的最大值，就将其设为最大值
        if(value>MAX_LIMIT){
            limit.setRowCount(new LongValue(MAX_LIMIT));
        }
    }

    /**
     * 表名统一处理
     */
    private String normalize(String name){
        return name.replace("`","")
                .replace("\"","")
                .trim()
                .toLowerCase();
    }


}
