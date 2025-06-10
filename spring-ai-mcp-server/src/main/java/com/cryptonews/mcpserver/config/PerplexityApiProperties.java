package com.cryptonews.mcpserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;

@Component
@ConfigurationProperties(prefix = "perplexity.api")
@Validated
@Data
public class PerplexityApiProperties {

    @NotEmpty
    private String url;
    @NotEmpty
    private String key;
    private String model = "sonar-pro";
    private String backupModel = "sonar";
} 