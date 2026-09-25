package com.datapilot.server;

import com.datapilot.server.config.DataSourceSecurityProperties;
import com.datapilot.server.security.DataSourcePasswordCrypto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestAESCrypto {
    @Autowired
    DataSourcePasswordCrypto dataSourcePasswordCrypto;

    @Test
    void textCrypto(){
        String plain_text1="123456";
        String plain_text2="123456";
        String cipher_text1=dataSourcePasswordCrypto.encrypt(plain_text1);
        System.out.println("cipher_text1:"+cipher_text1);
        String cipher_text2=dataSourcePasswordCrypto.encrypt(plain_text2);
        System.out.println("cipher_text2:"+cipher_text2);
    }
}
