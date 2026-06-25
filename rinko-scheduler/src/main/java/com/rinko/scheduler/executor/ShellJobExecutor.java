package com.rinko.scheduler.executor;

import com.rinko.infra.exception.InternalException;
import com.rinko.scheduler.model.entity.SchedulerJob;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

@Component
public class ShellJobExecutor implements JobExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Whitelist of allowed shell commands. Only commands in this set can be executed.
     */
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "echo", "curl", "wget", "python3", "python", "node", "java"
    );

    @Override
    public boolean supports(String type) {
        return "SHELL".equals(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String execute(SchedulerJob job) {
        try {
            Map<String, Object> config = objectMapper.readValue(job.getConfig(), Map.class);
            String command = (String) config.get("command");

            if (command == null || command.isBlank()) {
                throw new InternalException("Shell job command cannot be empty");
            }

            // Parse command into parts and validate against whitelist
            String[] parts = command.trim().split("\\s+");
            String executable = parts[0];

            if (!ALLOWED_COMMANDS.contains(executable)) {
                throw new InternalException("Command not allowed: " + executable +
                        ". Allowed commands: " + String.join(", ", ALLOWED_COMMANDS));
            }

            // Use ProcessBuilder with argument array instead of shell -c to prevent injection
            ProcessBuilder pb = new ProcessBuilder(parts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString().trim();
        } catch (InternalException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalException("Shell job failed: " + e.getMessage(), e);
        }
    }
}
