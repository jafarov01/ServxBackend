package com.servx.servx.service.Auth;

import com.servx.servx.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service("emailService")
public class EmailService {
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(User user, String token) {
        String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject("Email Verification");
        email.setText("Please verify your email by clicking the link: " + verificationUrl);
        mailSender.send(email);
    }
    @Async
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject("Servx Password Reset Request");
        email.setText("Hello " + name + ",\n\nYou requested a password reset. Please click the link below to set a new password. This link is valid for 1 hour.\n\n" + resetLink + "\n\nIf you did not request a password reset, please ignore this email.");
        try {
            mailSender.send(email);
        } catch (Exception ignored) {
        }
    }

    @Async
    public void sendPasswordResetConfirmationEmail(String to, String name) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject("Servx Password Reset Confirmation");
        email.setText("Hello " + name + ",\n\nYou have successfully reset your password.");
        try {
            mailSender.send(email);
        } catch (Exception ignored) {

        }
    }
}
