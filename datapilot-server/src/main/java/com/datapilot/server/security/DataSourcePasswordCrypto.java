package com.datapilot.server.security;

import com.datapilot.server.config.DataSourceSecurityProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class DataSourcePasswordCrypto {
    private static final String ALGORITHM="AES";
    private static final String TRANSFORMATION="AES/GCM/NoPadding";

    private static final int IV_LENGTH=12;
    private static final int TAG_LENGTH=128;

    private final SecretKeySpec secretKey;

    private final SecureRandom secureRandom = new SecureRandom();

    public DataSourcePasswordCrypto(DataSourceSecurityProperties properties){
        String key= properties.getDatasourceAesKey();

        if(key==null || key.getBytes(StandardCharsets.UTF_8).length!=32){
            throw new IllegalArgumentException("AES密钥长度必须为32位");
        }
        this.secretKey=new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),ALGORITHM);
    }

    public String encrypt(String plainText){
        try{
            byte[] iv =new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            GCMParameterSpec parameterSpec=new GCMParameterSpec(TAG_LENGTH,iv);

            cipher.init(Cipher.ENCRYPT_MODE,secretKey,parameterSpec);

            byte[] cipherText=cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] result=new byte[iv.length+cipherText.length];

            System.arraycopy(iv,0,result,0,iv.length);

            System.arraycopy(cipherText,0,result,iv.length,cipherText.length);

            return Base64.getEncoder().encodeToString(result);
        }catch (Exception e){
            throw new IllegalStateException("数据源密码加密失败",e);
        }
    }
    public String decrypt(String encryptedText){
        try{
            byte[] decoded= Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[IV_LENGTH];

            System.arraycopy(decoded,0,iv,0,IV_LENGTH);

            byte[] cipherText=new byte[decoded.length-IV_LENGTH];

            System.arraycopy(decoded,IV_LENGTH,cipherText,0,cipherText.length);

            Cipher cipher=Cipher.getInstance(TRANSFORMATION);

            GCMParameterSpec parameterSpec=new GCMParameterSpec(TAG_LENGTH,iv);

            cipher.init(Cipher.DECRYPT_MODE,secretKey,parameterSpec);

            byte[] plainText=cipher.doFinal(cipherText);

            return new String(plainText,StandardCharsets.UTF_8);
        }catch (Exception e){
            throw  new IllegalStateException("数据源密码解密失败",e);
        }
    }
}



