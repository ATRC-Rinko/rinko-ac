package com.rinko.scheduler.executor;

import com.rinko.infra.exception.InternalException;
import com.rinko.scheduler.model.entity.SchedulerJob;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@Component
public class BeanJobExecutor implements JobExecutor {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Whitelist of (beanName, methodName) pairs allowed to be invoked.
     */
    private static final Set<String> ALLOWED_BEANS = Set.of(
            // Add safe bean names here as needed
    );

    public BeanJobExecutor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supports(String type) {
        return "BEAN".equals(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String execute(SchedulerJob job) {
        try {
            Map<String, Object> config = objectMapper.readValue(job.getConfig(), Map.class);
            String beanName = (String) config.get("beanName");
            String methodName = (String) config.get("methodName");

            if (beanName == null || methodName == null) {
                throw new InternalException("Bean job requires beanName and methodName");
            }

            // Security: validate beanName is in whitelist
            if (!ALLOWED_BEANS.contains(beanName)) {
                throw new InternalException("Bean not allowed: " + beanName);
            }

            Object bean = applicationContext.getBean(beanName);
            Method method = bean.getClass().getMethod(methodName);

            // Security: only allow parameterless methods (no argument injection)
            if (method.getParameterCount() != 0) {
                throw new InternalException("Bean method must have no parameters: " + methodName);
            }

            Object result = method.invoke(bean);
            return result != null ? result.toString() : "OK";
        } catch (InternalException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalException("Bean job failed: " + e.getMessage(), e);
        }
    }
}
