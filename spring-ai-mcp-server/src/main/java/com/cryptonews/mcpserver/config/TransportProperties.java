package com.cryptonews.mcpserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.ai.mcp")
@Data
public class TransportProperties {

    private final Stdio stdio = new Stdio();
    private final Http http = new Http();
    private final Sse sse = new Sse();

    public Stdio getStdio() {
        return stdio;
    }

    public Http getHttp() {
        return http;
    }

    public Sse getSse() {
        return sse;
    }

    @Data
    public static class Stdio {
        private boolean enabled;
    }

    @Data
    public static class Http {
        private boolean enabled;
    }

    @Data
    public static class Sse {
        private boolean enabled;
    }
} 