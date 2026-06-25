package com.rinko.log.repository;

import com.rinko.log.dto.LogMessage;
import com.rinko.log.model.entity.LogEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ClickHouse 日志数据访问。
 */
@Repository
public class ClickHouseLogRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ClickHouseLogRepository(JdbcTemplate clickHouseJdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = clickHouseJdbcTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 批量插入日志到 ClickHouse。
     */
    public void batchInsert(List<LogMessage> messages) {
        String sql = "INSERT INTO logs (timestamp, level, service, traceId, spanId, class, message, thread, context, exception, exceptionClass) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (LogMessage msg : messages) {
            String contextJson = msg.context() != null ? objectMapper.writeValueAsString(msg.context()) : null;
            batchArgs.add(new Object[]{
                    Timestamp.valueOf(msg.timestamp().replace("T", " ").replace("Z", "")),
                    msg.level(),
                    msg.service(),
                    msg.traceId(),
                    msg.spanId(),
                    msg.className(),
                    msg.message(),
                    msg.thread(),
                    contextJson,
                    msg.exception(),
                    msg.exceptionClass()
            });
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * 查询日志，支持多条件过滤。
     */
    public List<LogEntry> queryLogs(String startTime, String endTime, String level,
                                    String service, String traceId, String keyword,
                                    int offset, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT timestamp, level, service, traceId, spanId, class, message, thread, context, exception, exceptionClass " +
                        "FROM logs WHERE timestamp >= ? AND timestamp <= ?");
        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(startTime.replace("T", " ")));
        params.add(Timestamp.valueOf(endTime.replace("T", " ")));

        if (level != null && !level.isEmpty()) {
            sql.append(" AND level = ?");
            params.add(level);
        }
        if (service != null && !service.isEmpty()) {
            sql.append(" AND service = ?");
            params.add(service);
        }
        if (traceId != null && !traceId.isEmpty()) {
            sql.append(" AND traceId = ?");
            params.add(traceId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND message LIKE ? ESCAPE '\\'");
            params.add("%" + escapeLike(keyword) + "%");
        }

        sql.append(" ORDER BY timestamp DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), rs -> {
            List<LogEntry> entries = new ArrayList<>();
            while (rs.next()) {
                LogEntry entry = new LogEntry();
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) entry.setTimestamp(ts.toLocalDateTime());
                entry.setLevel(rs.getString("level"));
                entry.setService(rs.getString("service"));
                entry.setTraceId(rs.getString("traceId"));
                entry.setSpanId(rs.getString("spanId"));
                entry.setClassName(rs.getString("class"));
                entry.setMessage(rs.getString("message"));
                entry.setThread(rs.getString("thread"));
                entry.setContext(rs.getString("context"));
                entry.setException(rs.getString("exception"));
                entry.setExceptionClass(rs.getString("exceptionClass"));
                entries.add(entry);
            }
            return entries;
        }, params.toArray());
    }

    /**
     * 统计日志条数。
     */
    public long countLogs(String startTime, String endTime, String level,
                          String service, String traceId, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT count(*) FROM logs WHERE timestamp >= ? AND timestamp <= ?");
        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(startTime.replace("T", " ")));
        params.add(Timestamp.valueOf(endTime.replace("T", " ")));

        if (level != null && !level.isEmpty()) {
            sql.append(" AND level = ?");
            params.add(level);
        }
        if (service != null && !service.isEmpty()) {
            sql.append(" AND service = ?");
            params.add(service);
        }
        if (traceId != null && !traceId.isEmpty()) {
            sql.append(" AND traceId = ?");
            params.add(traceId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND message LIKE ? ESCAPE '\\'");
            params.add("%" + escapeLike(keyword) + "%");
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0;
    }

    /**
     * Escape LIKE wildcard characters in user-supplied keyword.
     */
    private String escapeLike(String keyword) {
        return keyword.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
