package com.datapilot.server.datasource;

import com.datapilot.common.exception.BusinessException;
import com.datapilot.pojo.entity.DataSourceEntity;
import com.datapilot.server.mapper.DataSourceMapper;
import com.datapilot.server.security.DataSourcePasswordCrypto;
import com.datapilot.server.security.SecurityUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class ExternalDataSourceManager {
    private final DataSourceMapper dataSourceMapper;
    private final DataSourcePasswordCrypto crypto;

    /**
     * datasource->Hikari连接池
     */
    private final Map<Long, HikariDataSource> dataSourceCache=new ConcurrentHashMap<>();

    /**
     * 获取外部数据源连接池
     */
    public DataSource getDataSource(Long dataSourceId){
        return dataSourceCache.computeIfAbsent(
                dataSourceId,
                this::createDataSource
        );
    }

    /**
     * 创建一个新的Hikari连接池
     */
    private HikariDataSource createDataSource(Long dataSourceId){
        DataSourceEntity entity=dataSourceMapper.selectById(dataSourceId);
        if(entity==null){
            throw new BusinessException("数据源不存在");
        }
        if(!entity.getCreatedBy().equals(SecurityUtils.getCurrentId())){
            throw new BusinessException("无权访问该数据源");
        }
        String password=crypto.decrypt(entity.getPasswordCipher());
        String jdbcUrl=String.format(
                "jdbc:mysql://%s:%d/%s"+
                        "?connectTimeout=5000"+
                        "&socketTimeout=5000"+
                        "&useUnicode=true"+
                        "characterEncoding=UTF-8"+
                        "&serverTimezone=Asia/Shanghai"+
                        "&useInformationSchema=true",
                entity.getHost(),entity.getPort(),entity.getDatabaseName()
        );

        HikariConfig config=new HikariConfig();

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(entity.getUsername());
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName("external-ds-"+dataSourceId);

        //最大连接数
        config.setMaximumPoolSize(5);

        //不强制长期维持空闲连接
        config.setMinimumIdle(0);

        //从连接池获取连接最多等待5秒
        config.setConnectionTimeout(5000);

        //空闲连接最多保留60秒
        config.setIdleTimeout(60000);

        return new HikariDataSource(config);

    }

    /**
     * 删除指定连接池
     */
    public void remove(Long dataSourceId){
        HikariDataSource dataSource=dataSourceCache.remove(dataSourceId);

        if(dataSource!=null){
            dataSource.close();
        }

    }

    /**
     * 应用停止时关闭所有外部连接池
     */
    @PreDestroy
    public void closeAll(){
        for(HikariDataSource dataSource:dataSourceCache.values()){
            if (dataSource!=null){
                dataSource.close();
            }
        }
        dataSourceCache.clear();
    }

}
