package com.rinko.scheduler.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinko.infra.exception.InternalException;
import com.rinko.scheduler.entity.SchedulerJob;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@Component
public class ShellJobExecutor implements JobExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String type) { return "SHELL".equals(type); }

    @Override
    @SuppressWarnings("unchecked")
    public String execute(SchedulerJob job) {
        try {
            Map<String, Object> config = objectMapper.readValue(job.getConfig(), Map.class);
            String command = (String) config.get("command");
            Process process = new ProcessBuilder("sh", "-c", command).redirectErrorStream(true).start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString().trim();
        } catch (Exception e) {
            throw new InternalException("Shell job failed: " + e.getMessage(), e);
        }
    }
}
