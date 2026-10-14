package com.smartwater.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public boolean sendEmail(String to, String subject, String body) {
        log.info("[NOTIFICATION DISPATCH] Sending email to: {} | Subject: {}", to, subject);

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("[NOTIFICATION SUCCESS] Email sent to: {}", to);
                return true;
            } catch (Exception e) {
                log.warn("[NOTIFICATION FALLBACK] SMTP failed. Logged email locally for: {}", to, e);
                return false;
            }
        } else {
            log.info("[NOTIFICATION DEV MODE] JavaMailSender not configured. Email logged to console: \nTo: {}\nSubject: {}\nBody: {}", to, subject, body);
            return true;
        }
    }
}
