package com.mecano.notification_service.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${application.notification.from-email}")
    private String fromEmail;

    @Value("${application.notification.from-name}")
    private String fromName;

    // ── Email HTML ─────────────────────────────────────────────
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("✅ Email envoyé à {} : {}", to, subject);
        } catch (Exception e) {
            log.error("❌ Erreur envoi email à {} : {}", to, e.getMessage());
            throw new RuntimeException("Échec envoi email à " + to + " : " + e.getMessage(), e);
        }
    }

    // ── Email simple (texte) ───────────────────────────────────
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("✅ Email simple envoyé à {}", to);
        } catch (Exception e) {
            log.error("❌ Erreur email simple à {} : {}", to, e.getMessage());
            throw new RuntimeException("Échec envoi email simple à " + to + " : " + e.getMessage(), e);
        }
    }
}
