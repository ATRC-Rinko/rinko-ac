package com.rinko.scheduler.executor;

import com.rinko.infra.exception.InternalException;
import com.rinko.scheduler.model.entity.SchedulerJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShellJobExecutorTest {

    private ShellJobExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new ShellJobExecutor();
    }

    @Test
    void supports_shouldReturnTrueForSHELLType() {
        assertTrue(executor.supports("SHELL"));
        assertFalse(executor.supports("HTTP"));
    }

    @Test
    void execute_shouldRunAllowedEchoCommand() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{\"command\":\"echo hello world\"}");

        String result = executor.execute(job);
        assertEquals("hello world", result);
    }

    @Test
    void execute_shouldRejectDisallowedCommand() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{\"command\":\"rm -rf /\"}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("Command not allowed"));
    }

    @Test
    void execute_shouldRejectEmptyCommand() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{\"command\":\"\"}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("cannot be empty"));
    }

    @Test
    void execute_shouldRejectNullCommand() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("cannot be empty"));
    }

    @Test
    void execute_shouldRejectShellInjectionAttempt() {
        SchedulerJob job = new SchedulerJob();
        // Attempt to chain commands with semicolon
        job.setConfig("{\"command\":\"echo ok; rm -rf /\"}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        // The entire string is the command name — not in whitelist
        assertTrue(ex.getMessage().contains("Command not allowed"));
    }
}
