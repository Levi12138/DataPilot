package com.datapilot.server.ai.schema;

import com.datapilot.common.exception.BusinessException;
import com.datapilot.pojo.entity.MetadataColumn;
import com.datapilot.pojo.entity.MetadataTable;
import com.datapilot.server.mapper.MetadataColumnMapper;
import com.datapilot.server.mapper.MetadataTableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SchemaContextBuilder {
    private final MetadataTableMapper tableMapper;
    private final MetadataColumnMapper columnMapper;

    public String build(Long datasourceId){
        //查询这个数据源下的所有表
        List<MetadataTable> metadataTables=tableMapper.selectByDatasourceId(datasourceId);

        if(metadataTables==null || metadataTables.isEmpty()){
            throw new BusinessException("该数据源暂无元数据，请先同步");
        }

        StringBuilder schema=new StringBuilder();

        //遍历每张表
        for(MetadataTable table:metadataTables){

            appendTable(schema,table);

            //查询当前表的所有字段
            List<MetadataColumn> columns=columnMapper.selectByTableId(table.getId());

            //拼接字段
            if(columns!=null){
                for(MetadataColumn column:columns){
                    appendColumn(schema,column);
                }
            }

            schema.append("\n");
        }
        return schema.toString().trim();
    }

    private void appendTable(StringBuilder schema,MetadataTable table){
        schema.append("TABLE ");

        if(table.getSchemaName()!=null && !table.getSchemaName().isBlank()){
            schema.append(table.getSchemaName()).append(".");
        }

        schema.append(table.getTableName());

        if(table.getTableComment()!=null && !table.getSchemaName().isBlank()){
            schema.append("--").append(table.getTableComment());
        }

        schema.append("\n");
    }

    private void appendColumn(StringBuilder schema,MetadataColumn column){

        schema.append("- ").append(column.getColumnName()).append(" ").append(column.getDataType());

        if(Boolean.TRUE.equals(column.getPrimaryKey())){
            schema.append(" PRIMARY KEY");
        }

        if(Boolean.FALSE.equals(column.getNullable())){
            schema.append(" NOT NULL");
        }

        if(column.getColumnComment()!=null && !column.getColumnComment().isBlank()){
            schema.append(" -- ").append(column.getColumnComment());
        }

        schema.append("\n");
    }


}
