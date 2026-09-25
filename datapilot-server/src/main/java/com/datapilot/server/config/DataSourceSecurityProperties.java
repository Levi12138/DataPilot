package com.datapilot.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix="datapilot.security")
public class DataSourceSecurityProperties {
    private String datasourceAesKey;
}
