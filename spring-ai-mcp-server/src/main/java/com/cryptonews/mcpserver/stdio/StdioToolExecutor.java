package com.cryptonews.mcpserver.stdio;

import com.cryptonews.mcpserver.controller.ToolController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Function;

@Component
@Profile("stdio")
public class StdioToolExecutor implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StdioToolExecutor.class);

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    public StdioToolExecutor(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("StdioToolExecutor started. Listening for tool execution requests on stdin.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                ToolController.ToolExecutionRequest request = objectMapper.readValue(line, ToolController.ToolExecutionRequest.class);
                logger.info("Received tool execution request: {}", request);

                @SuppressWarnings("unchecked")
                Function<String, Mono<?>> function = (Function<String, Mono<?>>) applicationContext.getBean(request.toolName());

                Object result = function.apply(request.toolInput()).block();
                String jsonResult = objectMapper.writeValueAsString(result);

                System.out.println(jsonResult);
                logger.info("Tool execution successful. Result written to stdout.");
            } catch (Exception e) {
                logger.error("Error processing stdio request", e);
                System.out.println("{\"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }
} 