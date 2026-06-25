package com.rinko.scheduler.executor;

import com.rinko.infra.exception.InternalException;
import com.rinko.scheduler.model.entity.SchedulerJob;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Map;

@Component
public class HttpJobExecutor implements JobExecutor {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpJobExecutor(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public boolean supports(String type) {
        return "HTTP".equals(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String execute(SchedulerJob job) {
        try {
            Map<String, Object> config = objectMapper.readValue(job.getConfig(), Map.class);
            String url = (String) config.get("url");
            String method = (String) config.getOrDefault("method", "GET");

            if (url == null || url.isBlank()) {
                throw new InternalException("HTTP job URL cannot be empty");
            }

            // SSRF protection: validate URL target
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) {
                throw new InternalException("Invalid URL: no host");
            }
            if (!isAllowedHost(host)) {
                throw new InternalException("URL not allowed (internal/private network): " + host);
            }

            return switch (method.toUpperCase()) {
                case "POST" -> restTemplate.postForObject(url, null, String.class);
                case "PUT" -> {
                    restTemplate.put(url, null);
                    yield "OK";
                }
                default -> restTemplate.getForObject(url, String.class);
            };
        } catch (InternalException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalException("HTTP job failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate that a hostname does not resolve to a private/internal IP.
     */
    private boolean isAllowedHost(String host) {
        // Reject localhost variants
        if (host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1") || host.equals("::1")) {
            return false;
        }
        // Reject cloud metadata endpoints
        if (host.equals("169.254.169.254")) {
            return false;
        }
        try {
            InetAddress addr = InetAddress.getByName(host);
            return !addr.isLoopbackAddress()
                    && !addr.isLinkLocalAddress()
                    && !addr.isSiteLocalAddress(); // 10.x.x.x, 172.16-31.x.x, 192.168.x.x
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
