package com.student.portfolio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetEmail(String toEmail, String token, long expirationMinutes) {
        String subject = "Student Portfolio - Password Reset";
        String body = "Hello,\n\n"
                + "Use this reset token to set a new password:\n\n"
                + token + "\n\n"
                + "This token will expire in " + expirationMinutes + " minutes.\n\n"
                + "If you did not request this, you can ignore this email.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            logger.debug("Attempting to send reset email to: {}", toEmail);
            logger.debug("From email: {}", fromEmail);
            mailSender.send(message);
            logger.info("Password reset email sent successfully to {}", toEmail);
        } catch (MailException ex) {
            logger.error("Failed to send email to {} - Error: {}", toEmail, ex.getMessage());
            logger.error("Full exception:", ex);
            ex.printStackTrace();
            throw new IllegalStateException("Failed to send reset email. Check mail configuration. Error: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error sending email to {} - Error: {}", toEmail, ex.getMessage());
            logger.error("Full exception:", ex);
            ex.printStackTrace();
            throw new IllegalStateException("Unexpected error sending email: " + ex.getMessage());
        }
    }
}
