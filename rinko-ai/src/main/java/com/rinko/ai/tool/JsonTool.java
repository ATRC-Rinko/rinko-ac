package com.rinko.ai.tool;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * JSON 处理工具 — 供 Agent 调用。
 */
public class JsonTool {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Tool(
            name = "json_format",
            description = "将 JSON 字符串格式化输出（美化排版）",
            readOnly = true,
            concurrencySafe = true
    )
    public String formatJson(
            @ToolParam(name = "json", description = "需要格式化的 JSON 字符串")
            String json) {
        Object parsed = mapper.readValue(json, Object.class);
        return mapper.writeValueAsString(parsed);

    }

    @Tool(
            name = "json_query",
            description = "从 JSON 对象中按 key 提取值（支持嵌套 key，用 . 分隔，如 'user.name'）",
            readOnly = true,
            concurrencySafe = true
    )
    public String queryJson(
            @ToolParam(name = "json", description = "JSON 字符串")
            String json,
            @ToolParam(name = "key", description = "查询 key，嵌套用 . 分隔")
            String key) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = mapper.readValue(json, Map.class);
        String[] parts = key.split("\\.");
        Object current = map;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return "无法访问路径: " + key;
            }
        }
        return current != null ? current.toString() : "null";
    }
}
