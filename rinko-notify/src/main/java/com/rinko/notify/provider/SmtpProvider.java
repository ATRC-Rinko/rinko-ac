package com.rinko.notify.provider;

import com.rinko.infra.exception.InternalException;
import com.rinko.notify.service.NotificationAccountService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rinko.notify.email-provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(SmtpProvider.class);
    private final NotificationAccountService accountService;

    public Map<String, Object> smtpConfig() {
        return accountService.getEnabledConfig("SMTP");
    }

    public JavaMailSender buildAccount(Map<String, Object> config) {
        if (config == null) {
            log.warn("No SMTP account config found, mail sender disabled");
            return null;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost((String) config.get("host"));
        sender.setPort(config.get("port") instanceof Number ? ((Number) config.get("port")).intValue() : 587);
        sender.setUsername((String) config.get("username"));
        sender.setPassword((String) config.get("password"));
        Properties javaProps = sender.getJavaMailProperties();
        javaProps.put("mail.smtp.auth", "true");
        javaProps.put("mail.smtp.starttls.enable", "true");
        return sender;
    }

    @Override
    public void send(String to, String subject, String body) {
        Map<String, Object> config = smtpConfig();
        String from = (String) config.get("from");
        JavaMailSender mailSender = buildAccount(config);
        if (StringUtils.isBlank(from)) {
            log.warn("SMTP mail from is not configured, skipping email to {}", to);
            return;
        }
        if (mailSender == null) {
            log.warn("SMTP mail sender is not configured, skipping email to {}", to);
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Email sent to {} via SMTP", to);
        } catch (MessagingException e) {
            throw new InternalException(e.getMessage(), e);
        }
    }
}
