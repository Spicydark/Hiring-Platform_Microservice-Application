package com.hiringplatform.job_service.service;

import jakarta.mail.MessagingException; // Correct import for Jakarta Mail
import jakarta.mail.internet.MimeMessage; // Correct import for Jakarta Mail
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for sending emails.
 * Uses Spring Boot Mail starter for integration with JavaMailSender.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender; // Injected by Spring Boot Mail starter

    // Inject the sender email address from application properties
    @Value("${spring.mail.username}")
    private String fromEmailAddress;

    /**
     * Sends an email using the configured JavaMailSender.
     *
     * @param to      The recipient's email address.
     * @param subject The subject line of the email.
     * @param body    The content (body) of the email. Supports HTML if needed.
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true indicates multipart message (allows HTML)

            // Set the 'From' address explicitly
            helper.setFrom(fromEmailAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true indicates body is HTML, set to false for plain text

            mailSender.send(message);
            System.out.println("Email sent successfully to " + to); // Basic logging

        } catch (MessagingException e) {
            // In a real application, use a proper logging framework (like SLF4j)
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
            // Consider re-throwing a custom exception or handling the failure appropriately
            // throw new RuntimeException("Failed to send email", e);
        }
    }
}

