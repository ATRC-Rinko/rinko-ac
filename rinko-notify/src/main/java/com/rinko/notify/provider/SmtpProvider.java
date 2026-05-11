package com.rinko.notify.provider;

import com.rinko.notify.config.NotifyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@ConditionalOnProperty(name = "rinko.notify.email-provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(SmtpProvider.class);
    private final JavaMailSender mailSender;
    private final String from;

    public SmtpProvider(NotifyProperties props) {
        NotifyProperties.Smtp smtp = props.getSmtp();
        this.from = smtp.getFrom();
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(smtp.getHost());
        sender.setPort(smtp.getPort());
        sender.setUsername(smtp.getUsername());
        sender.setPassword(smtp.getPassword());
        Properties javaProps = sender.getJavaMailProperties();
        javaProps.put("mail.smtp.auth", "true");
        javaProps.put("mail.smtp.starttls.enable", "true");
        this.mailSender = sender;
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Email sent to {} via SMTP", to);
    }
}
