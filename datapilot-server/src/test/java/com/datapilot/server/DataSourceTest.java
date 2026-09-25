package com.datapilot.server;

import com.datapilot.common.result.Result;
import com.datapilot.server.datasource.ExternalDataSourceManager;
import com.datapilot.server.security.LoginUser;
import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;

@SpringBootTest
public class DataSourceTest {
    @Autowired
    private ExternalDataSourceManager dataSourceManager;

    @BeforeEach
    void setUp(){
        LoginUser user=new LoginUser(1L,"admin");
        Authentication authentication=new UsernamePasswordAuthenticationToken(
                user,
                null,
                Collections.emptyList()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testRemove() throws Exception{

        DataSource dataSource1=dataSourceManager.getDataSource(1L);
        dataSourceManager.remove(1L);
        DataSource dataSource2=dataSourceManager.getDataSource(1L);

        System.out.println(dataSource1==dataSource2);
    }
}
