package com.datapilot.server.service.impl;

import com.datapilot.common.exception.BusinessException;
import com.datapilot.common.result.Result;
import com.datapilot.pojo.entity.DataSourceEntity;
import com.datapilot.pojo.entity.MetadataColumn;
import com.datapilot.pojo.entity.MetadataTable;
import com.datapilot.pojo.vo.ColumnMetadataVO;
import com.datapilot.pojo.vo.TableMetadataVO;
import com.datapilot.server.datasource.ExternalDataSourceManager;
import com.datapilot.server.mapper.DataSourceMapper;
import com.datapilot.server.mapper.MetadataColumnMapper;
import com.datapilot.server.mapper.MetadataTableMapper;
import com.datapilot.server.security.SecurityUtils;
import com.datapilot.server.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;

@Service
@RequiredArgsConstructor
public class MetadataServiceImpl implements MetadataService {
    private final DataSourceMapper dataSourceMapper;
    private final MetadataTableMapper tableMapper;
    private final MetadataColumnMapper columnMapper;
    private final ExternalDataSourceManager dataSourceManager;
    private final JdbcTemplate jdbcTemplate;


    @Override
    @Transactional
    public void sync(Long datasourceId){
        //检查数据源及所有权
        DataSourceEntity entity=getOwnDataSource(datasourceId);

        if(entity.getStatus()==0){
            throw new BusinessException("数据源已禁用");
        }

        //获取外部连接池
        DataSource dataSource=dataSourceManager.getDataSource(datasourceId);

        try(Connection connection=dataSource.getConnection()){

            //获取数据库元数据对象
            DatabaseMetaData metaData=connection.getMetaData();

            //MySQL中的database更接近JDBC中的catalog
            String catalog=entity.getDatabaseName();

            //删除旧的元数据
            columnMapper.deleteByDatasourceId(datasourceId);
            tableMapper.deleteByDatasourceId(datasourceId);

            //查询所有普通表
            try(ResultSet tables=metaData.getTables(
                    catalog,
                    null,
                    "%",
                    new String[]{"TABLE"}
            )){
                while(tables.next()){
                    String tableName=tables.getString("TABLE_NAME");

                    String tableComment=tables.getString("REMARKS");

                    //保留表元数据
                    MetadataTable metadataTable=new MetadataTable();

                    metadataTable.setTableName(tableName);

                    metadataTable.setTableComment(tableComment);

                    metadataTable.setDatasourceId(datasourceId);

                    metadataTable.setSchemaName(catalog);

                    metadataTable.setLastSyncTime(LocalDateTime.now());

                    tableMapper.insert(metadataTable);

                    //查询这张表的主键
                    Set<String> primaryKeys=getPrimaryKeys(metaData,catalog,tableName);

                    //同步字段（将表字段存入字段表）
                    syncColumn(metaData,catalog,tableName,metadataTable.getId(),primaryKeys);
                }
            }


        } catch (SQLException e) {
            throw new BusinessException("数据源连接失败");
        }
    }

    /**
     * 数据源所有权校验
     */
    private DataSourceEntity getOwnDataSource(Long datasourceId){
        DataSourceEntity entity=dataSourceMapper.selectById(datasourceId);
        if(entity==null){
            throw new BusinessException("数据源不存在");
        }
        if(!entity.getCreatedBy().equals(SecurityUtils.getCurrentId())){
            throw new BusinessException("无权操作该数据源");
        }
        return entity;
    }

    /**
     * 读取主键
     */
    private Set<String> getPrimaryKeys(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        Set<String> primaryKeys=new HashSet<>();

        try(ResultSet rs=metaData.getPrimaryKeys(
                catalog,
                null,
                tableName
        )){
            while(rs.next()){
                String columnName=rs.getString("COLUMN_NAME");
                primaryKeys.add(columnName);
            }
        }
        return primaryKeys;
    }

    /**
     * 同步字段
     */
    private void syncColumn(
            DatabaseMetaData metaData,
            String catalog,
            String tableName,
            Long metadataTableId,
            Set<String> primaryKeys
    ) throws SQLException{
        try(ResultSet columns=metaData.getColumns(
                catalog,
                null,
                tableName,
                "%"
        )){
            while(columns.next()){
                String columnName=columns.getString("COLUMN_NAME");

                String dataType=columns.getString("TYPE_NAME");

                String columnComment=columns.getString("REMARKS");

                int nullableValue=columns.getInt("NULLABLE");

                int ordinalPosition=columns.getInt("ORDINAL_POSITION");

                MetadataColumn metadataColumn=new MetadataColumn();

                metadataColumn.setTableId(metadataTableId);

                metadataColumn.setColumnName(columnName);

                metadataColumn.setDataType(dataType);

                metadataColumn.setColumnComment(columnComment);

                metadataColumn.setNullable(nullableValue==DatabaseMetaData.columnNullable?1:0);

                metadataColumn.setPrimaryKey(primaryKeys.contains(columnName)?1:0);

                metadataColumn.setOrdinalPosition(ordinalPosition);

                columnMapper.insert(metadataColumn);

            }
        }
    }

    @Override
    public List<TableMetadataVO> listTables(Long datasourceId){
        DataSourceEntity entity=getOwnDataSource(datasourceId);

        if(entity.getStatus()==0){
            throw new BusinessException("数据源已禁用");
        }

        List<MetadataTable> tables=tableMapper.selectByDatasourceId(datasourceId);

        List<TableMetadataVO> tableMetadataVOS=new ArrayList<>();

        for(MetadataTable table:tables){
            TableMetadataVO vo=new TableMetadataVO();
            vo.setId(table.getId());
            vo.setTableName(table.getTableName());
            vo.setTableComment(table.getTableComment());
            vo.setSchemaName(table.getSchemaName());
            vo.setLastSyncTime(table.getLastSyncTime());
            vo.setDataSourceId(datasourceId);

            tableMetadataVOS.add(vo);
        }
        return tableMetadataVOS;
    }

    @Override
    public List<ColumnMetadataVO> listColumns(Long datasourceId,Long tableId){
        DataSourceEntity entity=getOwnDataSource(datasourceId);

        if(entity.getStatus()==0){
            throw new BusinessException("数据源已禁用");
        }

        List<MetadataColumn> metadataColumns=columnMapper.selectByTableId(tableId);

        if(metadataColumns==null || metadataColumns.isEmpty()){
            throw new BusinessException("元数据表不存在");
        }

        List<ColumnMetadataVO> columnMetadataVOS=new ArrayList<>();

        for(MetadataColumn column:metadataColumns){
            ColumnMetadataVO vo=new ColumnMetadataVO();
            vo.setId(column.getId());
            vo.setColumnName(column.getColumnName());
            vo.setColumnComment(column.getColumnComment());
            vo.setDataType(column.getDataType());
            vo.setTableId(column.getTableId());
            vo.setPrimaryKey(column.getPrimaryKey());
            vo.setNullable(column.getNullable());
            vo.setOrdinalPosition(column.getOrdinalPosition());

            columnMetadataVOS.add(vo);
        }
        return columnMetadataVOS;
    }


}
