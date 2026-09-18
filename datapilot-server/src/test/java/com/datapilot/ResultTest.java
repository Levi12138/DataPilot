package com.datapilot;

import com.datapilot.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ResultTest {
    @Test
    public void testResult(){
//        Result<String> result= Result.success("Datapilot");
//        System.out.println(result.getCode());
//        System.out.println(result.getData());
//        System.out.println(result.getMsg());

        Result result=Result.error("错误");
        System.out.println(result.getCode());
        System.out.println(result.getData());
        System.out.println(result.getMsg());
    }
}
