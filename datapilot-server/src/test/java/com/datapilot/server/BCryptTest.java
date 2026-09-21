package com.datapilot.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class BCryptTest {
    @Test
    public void testBCrypt(){
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        String password="123456";
        String passwordHash=encoder.encode(password);

        System.out.println(passwordHash);

        boolean result=encoder.matches(password,passwordHash);
        System.out.println(result);
    }
}
