package com.datapilot.server.controller;

import com.datapilot.common.result.Result;
import com.datapilot.pojo.dto.CreateDataSourceDTO;
import com.datapilot.pojo.dto.UpdateDataSourceDTO;
import com.datapilot.pojo.vo.ColumnMetadataVO;
import com.datapilot.pojo.vo.DataSourceVO;
import com.datapilot.pojo.vo.TableMetadataVO;
import com.datapilot.server.service.DataSourceService;
import com.datapilot.server.service.MetadataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {

    private final DataSourceService dataSourceService;
    private final MetadataService metadataService;

    @PostMapping
    public Result<Long> create(@RequestBody @Valid CreateDataSourceDTO dto){
        Long id = dataSourceService.create(dto);

        return Result.success(id);
    }

    @GetMapping
    public Result<List<DataSourceVO>> list(){
        List<DataSourceVO> vos=dataSourceService.list();
        return Result.success(vos);
    }

    @GetMapping("/{id}")
    public Result<DataSourceVO> selectById(@PathVariable("id") Long id){
        DataSourceVO vo=dataSourceService.getById(id);
        return Result.success(vo);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @RequestBody @Valid UpdateDataSourceDTO dto){
        dataSourceService.update(id,dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id){
        dataSourceService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/test-connection")
    public Result<String> testConnection(@PathVariable("id") Long id){
        dataSourceService.testConnection(id);
        return Result.success("连接成功");
    }

    @PostMapping("/{id}/metadata/sync")
    public Result<Void> syncMetadate(@PathVariable("id") Long id){
        metadataService.sync(id);
        return Result.success();
    }

    @GetMapping("/{id}/tables")
    public Result<List> tables(@PathVariable("id") Long datasourceId){
        List<TableMetadataVO> tableMetadataVOS=metadataService.listTables(datasourceId);
        return Result.success(tableMetadataVOS);
    }

    @GetMapping("/{id}/tables/{tableId}/columns")
    public Result<List> columns(@PathVariable("id") Long datasourceId,@PathVariable("tableId") Long tableId){
        List<ColumnMetadataVO> columnMetadataVOS=metadataService.listColumns(datasourceId,tableId);
        return Result.success(columnMetadataVOS);
    }
}
