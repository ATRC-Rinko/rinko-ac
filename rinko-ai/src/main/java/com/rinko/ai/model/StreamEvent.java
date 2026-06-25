package com.rinko.ai.model;

/**
 * 流式事件 DTO，封装 AgentScope streamEvents 的各类中间事件。
 *
 * @param type    事件类型：text | thinking | tool_call | tool_result | agent_end
 * @param delta   增量文本（text/thinking 类型时有效）
 * @param toolName 工具名称（tool_call/tool_result 类型时有效）
 * @param toolCallId 工具调用 ID
 */
public record StreamEvent(
        String type,
        String delta,
        String toolName,
        String toolCallId
) {

    public static StreamEvent text(String delta) {
        return new StreamEvent("text", delta, null, null);
    }

    public static StreamEvent thinking(String delta) {
        return new StreamEvent("thinking", delta, null, null);
    }

    public static StreamEvent toolCall(String toolName, String toolCallId) {
        return new StreamEvent("tool_call", null, toolName, toolCallId);
    }

    public static StreamEvent toolResult(String toolName, String toolCallId) {
        return new StreamEvent("tool_result", null, toolName, toolCallId);
    }

    public static StreamEvent agentEnd() {
        return new StreamEvent("agent_end", null, null, null);
    }
}
