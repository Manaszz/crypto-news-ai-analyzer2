package com.cryptonews.mcpserver.controller;

import com.cryptonews.mcpserver.tools.CryptoNewsTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
@Slf4j
public class McpController {

    private final CryptoNewsTools cryptoNewsTools;
    private final ObjectMapper objectMapper;

    @Autowired
    public McpController(CryptoNewsTools cryptoNewsTools) {
        this.cryptoNewsTools = cryptoNewsTools;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> handleMcpRequest(@RequestBody JsonNode request) {
        try {
            log.info("Received MCP request: {}", request);
            
            String method = request.get("method").asText();
            JsonNode params = request.has("params") ? request.get("params") : null;
            String id = request.has("id") ? request.get("id").asText() : "1";

            ObjectNode response = objectMapper.createObjectNode();
            response.put("jsonrpc", "2.0");
            response.put("id", id);

            switch (method) {
                case "initialize":
                    response.set("result", handleInitialize(params));
                    break;
                case "tools/list":
                    response.set("result", handleToolsList());
                    break;
                case "tools/call":
                    response.set("result", handleToolCall(params));
                    break;
                default:
                    ObjectNode error = objectMapper.createObjectNode();
                    error.put("code", -32601);
                    error.put("message", "Method not found: " + method);
                    response.set("error", error);
            }

            log.info("Sending MCP response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error handling MCP request", e);
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("jsonrpc", "2.0");
            ObjectNode error = objectMapper.createObjectNode();
            error.put("code", -32603);
            error.put("message", "Internal error: " + e.getMessage());
            errorResponse.set("error", error);
            return ResponseEntity.ok(errorResponse);
        }
    }

    private JsonNode handleInitialize(JsonNode params) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", "2024-11-05");
        
        ObjectNode capabilities = objectMapper.createObjectNode();
        ObjectNode tools = objectMapper.createObjectNode();
        tools.put("listChanged", false);
        capabilities.set("tools", tools);
        result.set("capabilities", capabilities);
        
        ObjectNode serverInfo = objectMapper.createObjectNode();
        serverInfo.put("name", "crypto-news-mcp-server");
        serverInfo.put("version", "1.0.0");
        result.set("serverInfo", serverInfo);
        
        return result;
    }

    private JsonNode handleToolsList() {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode tools = objectMapper.createArrayNode();
        
        // Get all @Tool annotated methods from CryptoNewsTools
        Method[] methods = CryptoNewsTools.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Tool.class)) {
                Tool toolAnnotation = method.getAnnotation(Tool.class);
                ObjectNode tool = objectMapper.createObjectNode();
                tool.put("name", toolAnnotation.name());
                tool.put("description", toolAnnotation.description());
                
                ObjectNode inputSchema = objectMapper.createObjectNode();
                inputSchema.put("type", "object");
                ObjectNode properties = objectMapper.createObjectNode();
                ArrayNode required = objectMapper.createArrayNode();
                
                // Add parameters
                Parameter[] parameters = method.getParameters();
                for (Parameter param : parameters) {
                    ObjectNode paramSchema = objectMapper.createObjectNode();
                    if (param.getType() == String.class) {
                        paramSchema.put("type", "string");
                    } else if (param.getType() == Integer.class || param.getType() == int.class) {
                        paramSchema.put("type", "integer");
                    } else {
                        paramSchema.put("type", "string");
                    }
                    properties.set(param.getName(), paramSchema);
                    
                    // Make string parameters required (except maxArticles, limit, timeRange)
                    if (param.getType() == String.class && 
                        !param.getName().equals("timeRange") && 
                        !param.getName().equals("keywords")) {
                        required.add(param.getName());
                    }
                }
                
                inputSchema.set("properties", properties);
                inputSchema.set("required", required);
                tool.set("inputSchema", inputSchema);
                
                tools.add(tool);
            }
        }
        
        result.set("tools", tools);
        return result;
    }

    private JsonNode handleToolCall(JsonNode params) throws Exception {
        String toolName = params.get("name").asText();
        JsonNode arguments = params.has("arguments") ? params.get("arguments") : objectMapper.createObjectNode();
        
        log.info("Calling tool: {} with arguments: {}", toolName, arguments);
        
        String result = callTool(toolName, arguments);
        
        ObjectNode response = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();
        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("type", "text");
        textContent.put("text", result);
        content.add(textContent);
        response.set("content", content);
        
        return response;
    }

    private String callTool(String toolName, JsonNode arguments) throws Exception {
        // Map tool calls to methods
        Map<String, String> toolMethodMap = new HashMap<>();
        toolMethodMap.put("getLatestCryptoNews", "getLatestCryptoNews");
        toolMethodMap.put("analyzeCryptocurrency", "analyzeCryptocurrency");
        toolMethodMap.put("getMarketSentiment", "getMarketSentiment");
        toolMethodMap.put("compareCryptocurrencies", "compareCryptocurrencies");
        toolMethodMap.put("getPositiveNews", "getPositiveNews");
        toolMethodMap.put("getNegativeNews", "getNegativeNews");
        toolMethodMap.put("getTrendForecast", "getTrendForecast");
        toolMethodMap.put("searchCryptoNews", "searchCryptoNews");
        toolMethodMap.put("getMarketMovingEvents", "getMarketMovingEvents");
        toolMethodMap.put("analyzeSentimentPriceCorrelation", "analyzeSentimentPriceCorrelation");

        String methodName = toolMethodMap.get(toolName);
        if (methodName == null) {
            return "{\"error\": \"Unknown tool: " + toolName + "\"}";
        }

        // Call the appropriate method based on tool name
        switch (toolName) {
            case "getLatestCryptoNews":
                String cryptocurrency = arguments.has("cryptocurrency") ? arguments.get("cryptocurrency").asText() : "BTC";
                Integer maxArticles = arguments.has("maxArticles") ? arguments.get("maxArticles").asInt() : 10;
                return cryptoNewsTools.getLatestCryptoNews(cryptocurrency, maxArticles);
                
            case "analyzeCryptocurrency":
                cryptocurrency = arguments.has("cryptocurrency") ? arguments.get("cryptocurrency").asText() : "BTC";
                String timeRange = arguments.has("timeRange") ? arguments.get("timeRange").asText() : "24 hours";
                return cryptoNewsTools.analyzeCryptocurrency(cryptocurrency, timeRange);
                
            case "getMarketSentiment":
                cryptocurrency = arguments.has("cryptocurrency") ? arguments.get("cryptocurrency").asText() : "BTC";
                timeRange = arguments.has("timeRange") ? arguments.get("timeRange").asText() : "24 hours";
                return cryptoNewsTools.getMarketSentiment(cryptocurrency, timeRange);
                
            case "compareCryptocurrencies":
                String cryptocurrencies = arguments.has("cryptocurrencies") ? arguments.get("cryptocurrencies").asText() : "BTC,ETH";
                return cryptoNewsTools.compareCryptocurrencies(cryptocurrencies);
                
            case "getPositiveNews":
                cryptocurrency = arguments.has("cryptocurrency") ? arguments.get("cryptocurrency").asText() : "BTC";
                Integer limit = arguments.has("limit") ? arguments.get("limit").asInt() : 10;
                return cryptoNewsTools.getPositiveNews(cryptocurrency, limit);
                
            case "getNegativeNews":
                cryptocurrency = arguments.has("cryptocurrency") ? arguments.get("cryptocurrency").asText() : "BTC";
                limit = arguments.has("limit") ? arguments.get("limit").asInt() : 10;
                return cryptoNewsTools.getNegativeNews(cryptocurrency, limit);
                
            case "getTrendForecast":
                cryptocurrency = arguments.has("cryptocurrency") ? arguments.get("cryptocurrency").asText() : "BTC";
                return cryptoNewsTools.getTrendForecast(cryptocurrency);
                
            case "searchCryptoNews":
                cryptocurrency = arguments.has("cryptocurrency") ? arguments.get("cryptocurrency").asText() : "BTC";
                String keywords = arguments.has("keywords") ? arguments.get("keywords").asText() : "";
                return cryptoNewsTools.searchCryptoNews(cryptocurrency, keywords);
                
            case "getMarketMovingEvents":
                cryptocurrency = arguments.has("cryptocurrency") ? arguments.get("cryptocurrency").asText() : "BTC";
                return cryptoNewsTools.getMarketMovingEvents(cryptocurrency);
                
            case "analyzeSentimentPriceCorrelation":
                cryptocurrency = arguments.has("cryptocurrency") ? arguments.get("cryptocurrency").asText() : "BTC";
                return cryptoNewsTools.analyzeSentimentPriceCorrelation(cryptocurrency);
                
            default:
                return "{\"error\": \"Unknown tool: " + toolName + "\"}";
        }
    }
} 