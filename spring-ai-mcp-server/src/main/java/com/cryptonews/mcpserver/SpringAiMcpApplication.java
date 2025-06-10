package com.cryptonews.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import com.cryptonews.mcpserver.tools.CryptoNewsTools;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SpringAiMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiMcpApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider tools(CryptoNewsTools cryptoNewsTools) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(cryptoNewsTools)
				.build();
	}
}
