package com.agile.ems.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MailService {
    @Value("${app.mail.from}")
    private String from;

        private final JavaMailSender emailSender;

        public void sendEmail(String toEmail, String subject, String body){
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            emailSender.send(message);

            System.out.println("Message sent successfully");
        }
    }
