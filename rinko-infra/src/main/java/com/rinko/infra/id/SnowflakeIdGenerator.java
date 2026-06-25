package com.rinko.infra.id;

import com.rinko.infra.exception.InternalException;
import org.springframework.stereotype.Component;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;

/**
 * 雪花算法 ID 生成器。
 * Worker ID 优先级：环境变量 SNOWFLAKE_WORKER_ID > MAC 地址哈希 > 随机数。
 * 在 Docker/K8s 环境中应通过 SNOWFLAKE_WORKER_ID 环境变量显式分配，避免 MAC 地址碰撞。
 */
@Component
public class SnowflakeIdGenerator {

    private static final long EPOCH = 1700000000000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator() {
        this(resolveWorkerId(), resolveDatacenterId());
    }

    /**
     * Resolve worker ID: env var SNOWFLAKE_WORKER_ID → MAC address hash → random.
     */
    private static long resolveWorkerId() {
        String envId = System.getenv("SNOWFLAKE_WORKER_ID");
        if (envId != null) {
            try {
                long id = Long.parseLong(envId.trim());
                if (id >= 0 && id <= MAX_WORKER_ID) return id;
            } catch (NumberFormatException ignored) {
            }
        }
        return computeWorkerIdFromMac();
    }

    /**
     * Resolve datacenter ID: env var SNOWFLAKE_DATACENTER_ID → default 1.
     */
    private static long resolveDatacenterId() {
        String envId = System.getenv("SNOWFLAKE_DATACENTER_ID");
        if (envId != null) {
            try {
                long id = Long.parseLong(envId.trim());
                if (id >= 0 && id <= MAX_DATACENTER_ID) return id;
            } catch (NumberFormatException ignored) {
            }
        }
        return 1;
    }

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId must be between 0 and " + MAX_WORKER_ID);
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId must be between 0 and " + MAX_DATACENTER_ID);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new InternalException("Clock moved backwards. Refusing to generate id.");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    private static long computeWorkerIdFromMac() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null && mac.length >= 6) {
                        return ((long) (mac[mac.length - 1] & 0xFF)) & MAX_WORKER_ID;
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall through to random
        }
        return new SecureRandom().nextInt((int) MAX_WORKER_ID + 1);
    }
}
