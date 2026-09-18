package com.datapilot;
import com.datapilot.mapper.TestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestMapperTest {
    @Autowired
    private TestMapper testMapper;

    @Test
    public void testGetNameById(){
        String name=testMapper.getNameById(1L);
        System.out.println(name);
    }
}
