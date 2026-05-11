package com.rinko.log.service;

import com.rinko.infra.exception.ValidationException;
import com.rinko.log.entity.LogLevelConfig;
import com.rinko.log.repository.LogLevelConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogLevelManagementServiceTest {

    @Mock
    private LogLevelConfigRepository logLevelConfigRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private BusProperties busProperties;

    @InjectMocks
    private LogLevelManagementService service;

    @Test
    void shouldThrowValidationExceptionForInvalidLevel() {
        assertThrows(ValidationException.class, () ->
                service.setLogLevel("rinko-auth", "com.rinko.auth", "INVALID"));
    }

    @Test
    void shouldUpdateExistingConfig() {
        LogLevelConfig existing = new LogLevelConfig();
        existing.setId(1L);
        existing.setServiceName("rinko-auth");
        existing.setLoggerName("com.rinko.auth");
        existing.setLogLevel("INFO");

        when(busProperties.getId()).thenReturn("rinko-log");
        when(logLevelConfigRepository.findByServiceNameAndLoggerName("rinko-auth", "com.rinko.auth"))
                .thenReturn(Optional.of(existing));
        when(logLevelConfigRepository.save(any(LogLevelConfig.class))).thenReturn(existing);

        LogLevelConfig result = service.setLogLevel("rinko-auth", "com.rinko.auth", "DEBUG");

        assertEquals("DEBUG", result.getLogLevel());
        verify(eventPublisher).publishEvent(any());
    }
}
