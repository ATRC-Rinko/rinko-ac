package com.rinko.infra.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    void shouldGenerateUniqueIds() {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(0, 1);
        long id1 = gen.nextId();
        long id2 = gen.nextId();
        assertNotEquals(id1, id2);
        assertTrue(id1 > 0);
        assertTrue(id2 > 0);
    }

    @Test
    void shouldGeneratePositiveIds() {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(0, 1);
        for (int i = 0; i < 100; i++) {
            assertTrue(gen.nextId() > 0);
        }
    }

    @Test
    void shouldRejectInvalidWorkerId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(32, 1));
    }

    @Test
    void shouldRejectInvalidDatacenterId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(0, -1));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(0, 32));
    }

    @Test
    void shouldUseDefaultConstructor() {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator();
        assertTrue(gen.nextId() > 0);
    }

    @Test
    void shouldBeMonotonicallyIncreasing() {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(0, 1);
        long prev = gen.nextId();
        for (int i = 0; i < 100; i++) {
            long next = gen.nextId();
            assertTrue(next > prev, "IDs should be monotonically increasing");
            prev = next;
        }
    }
}
