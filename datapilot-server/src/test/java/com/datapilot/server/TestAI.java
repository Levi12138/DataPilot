package com.datapilot.server;

import com.datapilot.server.ai.answer.AnswerGenerator;
import com.datapilot.server.ai.executor.QueryExecutor;
import com.datapilot.server.ai.model.GeneratedSql;
import com.datapilot.server.ai.model.ValidatedSql;
import com.datapilot.server.ai.schema.SchemaContextBuilder;
import com.datapilot.server.ai.service.impl.AiModelServiceImpl;
import com.datapilot.server.ai.sql.SqlGenerator;
import com.datapilot.server.ai.sql.SqlValidator;
import com.datapilot.server.security.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestAI {

    @Autowired
    SqlGenerator generator;

    @Autowired
    SchemaContextBuilder builder;

    @Autowired
    SqlValidator validator;

    @Autowired
    QueryExecutor executor;

    @Autowired
    AnswerGenerator answerGenerator;

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
    public void testAI(){
        GeneratedSql result=generator.generate(builder.build(1L),"一共有多少条订单？");

        System.out.println(result);

        ValidatedSql validatedSql=validator.validate(1L,result);

        List<Map<String,Object>> sqlResult=executor.execute(1L,validatedSql);

        System.out.println(answerGenerator.generate("一共有多少条订单？",validatedSql.getSql(),sqlResult));




    }
}
