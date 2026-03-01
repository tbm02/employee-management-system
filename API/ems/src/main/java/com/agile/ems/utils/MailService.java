package com.agile.ems.utils;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Value("${app.mail.from}")
    private String from;

    private final JavaMailSender emailSender;

    /**
     * Sends an email asynchronously so the calling thread is never blocked.
     * Failures are logged as warnings — they do NOT propagate back to the caller.
     */
    @Async
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            emailSender.send(message);
            log.info("Email sent to {}: {}", toEmail, subject);
        } catch (Exception ex) {
            log.warn("Failed to send email to {} [{}]: {}", toEmail, subject, ex.getMessage());
        }
    }
}
