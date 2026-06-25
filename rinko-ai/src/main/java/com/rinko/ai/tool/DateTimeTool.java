package com.rinko.ai.tool;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具 — 供 Agent 调用。
 */
public class DateTimeTool {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(
            name = "get_current_time",
            description = "获取当前日期时间，支持指定时区（IANA 时区名，如 Asia/Shanghai）",
            readOnly = true,
            concurrencySafe = true
    )
    public String getCurrentTime(
            @ToolParam(name = "timezone", description = "IANA 时区名，例如 Asia/Shanghai、America/New_York")
            String timezone) {
        ZoneId zone = timezone != null && !timezone.isBlank()
                ? ZoneId.of(timezone)
                : ZoneId.systemDefault();
        return LocalDateTime.now(zone).format(FORMATTER) + " [" + zone + "]";
    }

    @Tool(
            name = "get_current_date",
            description = "获取当前日期",
            readOnly = true,
            concurrencySafe = true
    )
    public String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Tool(
            name = "calculate_date",
            description = "计算从今天起偏移 N 天后的日期，N 可以是负数表示过去",
            readOnly = true,
            concurrencySafe = true
    )
    public String calculateDate(
            @ToolParam(name = "offsetDays", description = "偏移天数，正数表示未来，负数表示过去")
            int offsetDays) {
        LocalDate target = LocalDate.now().plusDays(offsetDays);
        return target.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
