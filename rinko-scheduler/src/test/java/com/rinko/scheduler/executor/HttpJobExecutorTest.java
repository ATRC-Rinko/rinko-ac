package com.rinko.scheduler.executor;

import com.rinko.infra.exception.InternalException;
import com.rinko.scheduler.model.entity.SchedulerJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static org.junit.jupiter.api.Assertions.*;

class HttpJobExecutorTest {

    private HttpJobExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new HttpJobExecutor(new RestTemplateBuilder());
    }

    @Test
    void supports_shouldReturnTrueForHTTPType() {
        assertTrue(executor.supports("HTTP"));
        assertFalse(executor.supports("SHELL"));
    }

    @Test
    void execute_shouldRejectLoopbackUrl() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{\"url\":\"http://127.0.0.1:8080/admin\"}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("not allowed"));
    }

    @Test
    void execute_shouldRejectLocalhostUrl() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{\"url\":\"http://localhost:8080/admin\"}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("not allowed"));
    }

    @Test
    void execute_shouldRejectCloudMetadataEndpoint() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{\"url\":\"http://169.254.169.254/latest/meta-data/\"}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("not allowed"));
    }

    @Test
    void execute_shouldRejectPrivateIp() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{\"url\":\"http://192.168.1.1/api\"}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("not allowed"));
    }

    @Test
    void execute_shouldRejectEmptyUrl() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("cannot be empty"));
    }
}
