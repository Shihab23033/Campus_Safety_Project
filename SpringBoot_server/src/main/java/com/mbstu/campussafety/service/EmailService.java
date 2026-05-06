package com.mbstu.campussafety.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.mbstu.campussafety.config.MailConfig;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final MailConfig mailConfig;

    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailConfig.getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            log.debug("Sending email to: {}", to);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email to " + to + ": " + e.getMessage(), e);
        }
    }

    public void sendOtpEmail(String to, String otp) {
        String subject = "Campus Safety - Email Verification";
        String body = "Your OTP for Campus Safety email verification is: " + otp + "\n\n" +
                      "This OTP is valid for 10 minutes.\n\n" +
                      "Do not share this OTP with anyone.\n\n" +
                      "Campus Safety Team";
        sendSimpleEmail(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "Campus Safety - Password Reset Request";
        String body = "You have requested to reset your password. " +
                      "Please use the following link to reset your password:\n\n" +
                      resetToken + "\n\n" +
                      "This link is valid for 24 hours.\n\n" +
                      "If you did not request this, please ignore this email.\n\n" +
                      "Campus Safety Team";
        sendSimpleEmail(to, subject, body);
    }

    public void sendAlertNotificationEmail(String to, String alertTitle, String alertDescription) {
        String subject = "Campus Safety Alert Notification";
        String body = "An emergency alert has been triggered:\n\n" +
                      "Alert: " + alertTitle + "\n" +
                      "Description: " + alertDescription + "\n\n" +
                      "Please check the Campus Safety app for more details.\n\n" +
                      "Campus Safety Team";
        sendSimpleEmail(to, subject, body);
    }
}
