package com.rinko.scheduler.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinko.infra.exception.InternalException;
import com.rinko.scheduler.model.entity.SchedulerJob;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class HttpJobExecutor implements JobExecutor {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String type) { return "HTTP".equals(type); }

    @Override
    @SuppressWarnings("unchecked")
    public String execute(SchedulerJob job) {
        try {
            Map<String, Object> config = objectMapper.readValue(job.getConfig(), Map.class);
            String url = (String) config.get("url");
            String method = (String) config.getOrDefault("method", "GET");
            return switch (method.toUpperCase()) {
                case "POST" -> restTemplate.postForObject(url, null, String.class);
                case "PUT" -> { restTemplate.put(url, null); yield "OK"; }
                default -> restTemplate.getForObject(url, String.class);
            };
        } catch (Exception e) {
            throw new InternalException("HTTP job failed: " + e.getMessage(), e);
        }
    }
}
