package com.rinko.scheduler.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinko.scheduler.entity.SchedulerJob;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component
public class BeanJobExecutor implements JobExecutor {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BeanJobExecutor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supports(String type) { return "BEAN".equals(type); }

    @Override
    @SuppressWarnings("unchecked")
    public String execute(SchedulerJob job) {
        try {
            Map<String, Object> config = objectMapper.readValue(job.getConfig(), Map.class);
            String beanName = (String) config.get("beanName");
            String methodName = (String) config.get("methodName");
            Object bean = applicationContext.getBean(beanName);
            Method method = bean.getClass().getMethod(methodName);
            Object result = method.invoke(bean);
            return result != null ? result.toString() : "OK";
        } catch (Exception e) {
            throw new RuntimeException("Bean job failed: " + e.getMessage(), e);
        }
    }
}
