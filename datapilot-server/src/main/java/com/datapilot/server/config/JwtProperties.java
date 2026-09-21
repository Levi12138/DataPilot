package com.datapilot.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "datapilot.jwt")
public class JwtProperties {
    private String secret;
    private Long expiration;
}
