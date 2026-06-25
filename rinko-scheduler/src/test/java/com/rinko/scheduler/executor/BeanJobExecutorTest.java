package com.rinko.scheduler.executor;

import com.rinko.infra.exception.InternalException;
import com.rinko.scheduler.model.entity.SchedulerJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BeanJobExecutorTest {

    private ApplicationContext ctx;
    private BeanJobExecutor executor;

    @BeforeEach
    void setUp() {
        ctx = mock(ApplicationContext.class);
        executor = new BeanJobExecutor(ctx);
    }

    @Test
    void supports_shouldReturnTrueForBEANType() {
        assertTrue(executor.supports("BEAN"));
        assertFalse(executor.supports("HTTP"));
    }

    @Test
    void execute_shouldRejectBeanNotInWhitelist() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{\"beanName\":\"dataSource\",\"methodName\":\"getConnection\"}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("not allowed"));
    }

    @Test
    void execute_shouldRejectNullBeanName() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{\"methodName\":\"test\"}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("requires beanName and methodName"));
    }

    @Test
    void execute_shouldRejectNullMethodName() {
        SchedulerJob job = new SchedulerJob();
        job.setConfig("{\"beanName\":\"testBean\"}");

        InternalException ex = assertThrows(InternalException.class, () -> executor.execute(job));
        assertTrue(ex.getMessage().contains("requires beanName and methodName"));
    }
}
